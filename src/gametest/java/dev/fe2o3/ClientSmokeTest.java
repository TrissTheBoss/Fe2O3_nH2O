package dev.fe2o3;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.nio.file.Files;
import java.nio.file.Path;

/** Real client/Mixin smoke coverage. Screenshots are evidence, not parity baselines. */
public final class ClientSmokeTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            long rendererGeneration = Blaze3DDeviceLifecycle.generation();
            if (rendererGeneration == 0) throw new AssertionError("Blaze3D renderer lifecycle was not initialized");
            AtomicInteger ready = new AtomicInteger();
            AtomicInteger lost = new AtomicInteger();
            AtomicReference<GpuBuffer> ownedBuffer = new AtomicReference<>();
            Blaze3DDeviceLifecycle.Resource probe = new Blaze3DDeviceLifecycle.Resource() {
                @Override
                public void onDeviceReady(com.mojang.blaze3d.systems.GpuDevice device, long generation) {
                    if (generation != rendererGeneration) throw new AssertionError("Wrong Blaze3D device generation");
                    GpuBuffer buffer = device.createBuffer(
                            () -> "Fe2O3 lifecycle smoke test", GpuBuffer.USAGE_COPY_DST, 4);
                    if (!ownedBuffer.compareAndSet(null, buffer)) {
                        buffer.close();
                        throw new AssertionError("Lifecycle resource initialized more than once");
                    }
                    ready.incrementAndGet();
                }

                @Override
                public void onDeviceLost(com.mojang.blaze3d.systems.GpuDevice device) {
                    GpuBuffer buffer = ownedBuffer.getAndSet(null);
                    if (buffer != null) buffer.close();
                    lost.incrementAndGet();
                }
            };
            Blaze3DDeviceLifecycle.register(probe);
            Blaze3DDeviceLifecycle.register(probe);
            if (ready.get() != 1) throw new AssertionError("Resource did not attach exactly once to active Blaze3D device");
            GpuBuffer allocatedBuffer = ownedBuffer.get();
            if (allocatedBuffer == null || allocatedBuffer.isClosed()) {
                throw new AssertionError("Resource did not create a live Blaze3D buffer");
            }
            Blaze3DDeviceLifecycle.unregister(probe);
            if (lost.get() != 1) throw new AssertionError("Resource did not detach exactly once from active Blaze3D device");
            if (ownedBuffer.get() != null || !allocatedBuffer.isClosed()) {
                throw new AssertionError("Resource did not close its Blaze3D buffer on detach");
            }

            long before = Mipmaps.completed();
            try (NativeImage base = new NativeImage(16, 16, true)) {
                for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) base.setPixel(x, y, 0xffffffff);
                NativeImage[] levels = MipmapGenerator.generateMipLevels(
                        Identifier.fromNamespaceAndPath("fe2o3", "integration"), new NativeImage[]{base},
                        4, MipmapStrategy.MEAN, 0.0f, new Transparency(false, false));
                try {
                    if (Mipmaps.completed() != before + 1) throw new AssertionError("Live Mixin did not execute WebGPU");
                    if (levels[4].getPixel(0, 0) != 0xffffffff) throw new AssertionError("Live mip pixel mismatch");
                } finally {
                    for (int i = 1; i < levels.length; i++) levels[i].close();
                }
            }
        });
        long beforeVanillaFallback = Mipmaps.completed();
        try (NativeImage base = new NativeImage(16, 16, false)) {
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                base.setPixel(x, y, ((x + y) & 1) == 0 ? 0xffffffff : 0x00ffffff);
            }
            NativeImage[] levels = MipmapGenerator.generateMipLevels(
                    Identifier.fromNamespaceAndPath("fe2o3", "cutout-fallback"),
                    new NativeImage[]{base}, 4, MipmapStrategy.CUTOUT, 0.0f,
                    new Transparency(true, true));
            try {
                if (Mipmaps.completed() != beforeVanillaFallback) {
                    throw new AssertionError("CUTOUT strategy must stay on vanilla");
                }
                if (levels.length != 5) throw new AssertionError("Vanilla CUTOUT chain has the wrong length");
            } finally {
                for (int i = 1; i < levels.length; i++) levels[i].close();
            }
        }
        try (var world = context.worldBuilder().create()) {
            var server = world.getServer();
            server.runCommand("fill -8 99 -8 8 99 8 minecraft:stone");
            server.runCommand("setblock -2 100 0 minecraft:stone");
            server.runCommand("setblock -1 100 0 minecraft:oak_stairs");
            server.runCommand("setblock 0 100 0 minecraft:oak_slab");
            server.runCommand("setblock 1 100 0 minecraft:glass");
            server.runCommand("setblock 2 100 0 minecraft:oak_leaves");
            server.runCommand("summon minecraft:pig 3 100 0 {NoAI:1b}");
            server.runCommand("tp @p 0 100 6 180 10");
            server.runCommand("time set noon");
            context.waitFor(client -> client.level != null
                    && client.level.getBlockState(new BlockPos(-1, 100, 0)).is(Blocks.OAK_STAIRS), 1200);
            world.getConnection().waitForChunksRender();
            server.runCommand("particle minecraft:flame 0 101 0 0.5 0.5 0.5 0 80 force");
            context.waitTicks(2);
            context.takeScreenshot("terrain-partial-blocks-entity-particles-sky");
            long beforeReload = Mipmaps.completed();
            var reload = context.computeOnClient(client -> client.reloadResourcePacks());
            context.waitFor(client -> reload.isDone(), 1200);
            reload.join();
            if (Mipmaps.completed() <= beforeReload) throw new AssertionError("Reload did not regenerate GPU mipmaps");
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("after-resource-reload");
        }
        try {
            Files.writeString(Path.of("FE2O3_CLIENT_TEST_PASSED"), "mixin, scene and resource reload passed\n");
        } catch (java.io.IOException e) {
            throw new AssertionError("Unable to write client test completion marker", e);
        }
        System.out.println("[Fe2O3] CLIENT_TEST_COMPLETE");
    }
}

