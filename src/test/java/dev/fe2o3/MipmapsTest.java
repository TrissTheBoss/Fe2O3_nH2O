package dev.fe2o3;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class MipmapsTest {
    private static void requireGpu() {
        boolean available = Mipmaps.initialize();
        if (Boolean.getBoolean("fe2o3.requireGpu")) assertTrue(available, "CI requires a working WebGPU adapter");
        Assumptions.assumeTrue(available, "No bundled native or GPU; CI must run with FE2O3_REQUIRE_GPU=true");
    }

    @Test void platformMapping() {
        assertEquals("linux-x86_64", NativeLoader.platform("Linux", "amd64"));
        assertEquals("windows-x86_64", NativeLoader.platform("Windows 11", "x86_64"));
        assertEquals("macos-aarch64", NativeLoader.platform("Mac OS X", "arm64"));
        assertThrows(IllegalStateException.class, () -> NativeLoader.platform("Plan9", "arm64"));
    }

    @Test void recoveredTablesMatchActualMinecraftArithmetic() {
        int[] tables = Mipmaps.colorTables();
        Random random = new Random(262);
        for (int i = 0; i < 100_000; i++) {
            int a = random.nextInt(), b = random.nextInt(), c = random.nextInt(), d = random.nextInt();
            int result = (((a >>> 24) + (b >>> 24) + (c >>> 24) + (d >>> 24)) / 4) << 24;
            for (int shift : new int[]{0, 8, 16}) {
                int sum = tables[(a >>> shift) & 255] + tables[(b >>> shift) & 255]
                        + tables[(c >>> shift) & 255] + tables[(d >>> shift) & 255];
                result |= tables[256 + sum / 4] << shift;
            }
            assertEquals(ARGB.meanLinear(a, b, c, d), result);
        }
    }

    @Test void gpuChainsArePixelExactAgainstVanilla() {
        requireGpu();
        Random random = new Random(0xFE203);
        for (int[] shape : new int[][]{{2, 2, 1}, {16, 16, 4}, {15, 10, 3}, {64, 256, 5}, {257, 129, 6}, {512, 512, 9}}) {
            for (int pattern = 0; pattern < 4; pattern++) {
                try (NativeImage base = new NativeImage(shape[0], shape[1], false)) {
                    for (int y = 0; y < shape[1]; y++) for (int x = 0; x < shape[0]; x++) {
                        int pixel = switch (pattern) {
                            case 0 -> random.nextInt() | 0xff000000;
                            case 1 -> random.nextInt();
                            case 2 -> ((x + y) & 1) == 0 ? 0xffffffff : 0;
                            default -> 0xff7f80ff;
                        };
                        base.setPixel(x, y, pixel);
                    }
                    int[] before = base.getPixels();
                    NativeImage[] gpu = Mipmaps.tryGenerate(new NativeImage[]{base}, shape[2], MipmapStrategy.MEAN, new Transparency(true, true));
                    assertNotNull(gpu, "Eligible images must actually take the GPU path");
                    NativeImage[] vanilla = MipmapGenerator.generateMipLevels(Identifier.fromNamespaceAndPath("fe2o3", "test"),
                            new NativeImage[]{base}, shape[2], MipmapStrategy.MEAN, 0.0f, new Transparency(true, true));
                    try {
                        assertSame(base, gpu[0]);
                        assertArrayEquals(before, base.getPixels(), "Source ownership and content must be preserved");
                        for (int level = 1; level < gpu.length; level++) {
                            assertEquals(vanilla[level].getWidth(), gpu[level].getWidth());
                            assertEquals(vanilla[level].getHeight(), gpu[level].getHeight());
                            assertArrayEquals(vanilla[level].getPixels(), gpu[level].getPixels(), "pattern=" + pattern + " level=" + level);
                        }
                    } finally {
                        for (int level = 1; level < gpu.length; level++) { gpu[level].close(); vanilla[level].close(); }
                    }
                }
            }
        }
        assertTrue(Mipmaps.completed() >= 24);
    }


    @Test void opaqueAutoStrategyMatchesVanillaOnGpu() {
        requireGpu();
        try (NativeImage base = new NativeImage(16, 16, false)) {
            Random random = new Random(0xA070);
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                base.setPixel(x, y, random.nextInt() | 0xff000000);
            }
            int[] before = base.getPixels();
            long completed = Mipmaps.completed();
            NativeImage[] gpu = Mipmaps.tryGenerate(new NativeImage[]{base}, 4,
                    MipmapStrategy.AUTO, new Transparency(false, false));
            assertNotNull(gpu, "Opaque AUTO textures should use the eligible MEAN GPU path");
            assertEquals(completed + 1, Mipmaps.completed());
            NativeImage[] vanilla = MipmapGenerator.generateMipLevels(
                    Identifier.fromNamespaceAndPath("fe2o3", "auto"), new NativeImage[]{base},
                    4, MipmapStrategy.AUTO, 0.0f, new Transparency(false, false));
            try {
                assertEquals(vanilla.length, gpu.length);
                assertArrayEquals(before, base.getPixels(), "The source image must remain unchanged");
                for (int level = 1; level < gpu.length; level++) {
                    assertArrayEquals(vanilla[level].getPixels(), gpu[level].getPixels(),
                            "AUTO output mismatch at level " + level);
                }
            } finally {
                for (int level = 1; level < gpu.length; level++) {
                    gpu[level].close();
                    vanilla[level].close();
                }
            }
        }
    }

    @Test void specializedPackStrategiesAndSuppliedMipsFallBackWithoutMutation() {
        try (NativeImage base = new NativeImage(16, 16, true); NativeImage supplied = new NativeImage(8, 8, true)) {
            base.setPixel(0, 0, 0x017f2345);
            int[] before = base.getPixels();
            for (MipmapStrategy strategy : new MipmapStrategy[]{MipmapStrategy.CUTOUT, MipmapStrategy.STRICT_CUTOUT, MipmapStrategy.DARK_CUTOUT, MipmapStrategy.AUTO}) {
                assertNull(Mipmaps.tryGenerate(new NativeImage[]{base}, 4, strategy, new Transparency(true, true)));
                assertArrayEquals(before, base.getPixels());
            }
            assertNull(Mipmaps.tryGenerate(new NativeImage[]{base, supplied}, 4, MipmapStrategy.MEAN, new Transparency(false, false)));
            assertNull(Mipmaps.tryGenerate(new NativeImage[]{base}, 0, MipmapStrategy.MEAN, new Transparency(false, false)));
            assertNull(Mipmaps.tryGenerate(new NativeImage[]{base}, 5, MipmapStrategy.MEAN, new Transparency(false, false)));
        }
    }

    @Test void jniRejectsMalformedRequestsAndRemainsUsable() {
        requireGpu();
        assertThrows(IllegalStateException.class, () -> NativeBridge.generate(new int[4], -1, 2, 1));
        assertThrows(IllegalStateException.class, () -> NativeBridge.generate(new int[3], 2, 2, 1));
        assertThrows(IllegalStateException.class, () -> NativeBridge.generate(new int[4], 2, 2, 2));
        assertArrayEquals(new int[]{0xffffffff}, NativeBridge.generate(new int[]{-1, -1, -1, -1}, 2, 2, 1));
    }
}

