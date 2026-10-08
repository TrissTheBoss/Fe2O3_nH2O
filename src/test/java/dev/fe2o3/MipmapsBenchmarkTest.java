package dev.fe2o3;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/** Opt-in, informational end-to-end comparison; never asserts a speedup. */
class MipmapsBenchmarkTest {
    private static final int WARMUP_ITERATIONS = 4;
    private static final int SAMPLE_ITERATIONS = 20;

    @Test
    void compareVanillaAndWebGpuEndToEndWhenRequested() {
        Assumptions.assumeTrue(Boolean.getBoolean("fe2o3.benchmarkMipmaps"),
                "Set -Dfe2o3.benchmarkMipmaps=true to run the informational benchmark");
        Assumptions.assumeTrue(Mipmaps.initialize(),
                "The benchmark requires the bundled native library and an available WebGPU adapter");

        for (int size : new int[]{128, 512, 1024}) {
            benchmark(size);
        }
    }

    private static void benchmark(int size) {
        int levels = Integer.numberOfTrailingZeros(size);
        NativeImage base = new NativeImage(size, size, false);
        Random random = new Random(0xFE203L + size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                base.setPixel(x, y, random.nextInt() | 0xff000000);
            }
        }

        try (base) {
            Supplier<NativeImage[]> vanilla = () -> MipmapGenerator.generateMipLevels(
                    Identifier.fromNamespaceAndPath("fe2o3", "benchmark"),
                    new NativeImage[]{base}, levels, MipmapStrategy.MEAN, 0.0f,
                    new Transparency(false, false));
            Supplier<NativeImage[]> webgpu = () -> {
                NativeImage[] result = Mipmaps.tryGenerate(new NativeImage[]{base}, levels,
                        MipmapStrategy.MEAN, new Transparency(false, false));
                if (result == null) throw new AssertionError("Eligible benchmark image fell back from WebGPU");
                return result;
            };

            assertEquivalent(vanilla.get(), webgpu.get());
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                closeGenerated(vanilla.get());
                closeGenerated(webgpu.get());
            }

            List<Long> vanillaSamples = new ArrayList<>(SAMPLE_ITERATIONS);
            List<Long> webgpuSamples = new ArrayList<>(SAMPLE_ITERATIONS);
            for (int i = 0; i < SAMPLE_ITERATIONS; i++) {
                if ((i & 1) == 0) {
                    vanillaSamples.add(time(vanilla));
                    webgpuSamples.add(time(webgpu));
                } else {
                    webgpuSamples.add(time(webgpu));
                    vanillaSamples.add(time(vanilla));
                }
            }

            System.out.printf(Locale.ROOT,
                    "[Fe2O3 benchmark] %dx%d levels=%d vanilla median/p95=%.2f/%.2f ms; "
                            + "WebGPU end-to-end median/p95=%.2f/%.2f ms%n",
                    size, size, levels, medianMs(vanillaSamples), percentile95Ms(vanillaSamples),
                    medianMs(webgpuSamples), percentile95Ms(webgpuSamples));
        }
    }

    private static long time(Supplier<NativeImage[]> generator) {
        long start = System.nanoTime();
        NativeImage[] generated = generator.get();
        long elapsed = System.nanoTime() - start;
        closeGenerated(generated);
        return elapsed;
    }

    private static void assertEquivalent(NativeImage[] vanilla, NativeImage[] webgpu) {
        try {
            if (vanilla.length != webgpu.length) throw new AssertionError("Mipmap chain lengths differ");
            for (int level = 0; level < vanilla.length; level++) {
                if (!Arrays.equals(vanilla[level].getPixels(), webgpu[level].getPixels())) {
                    throw new AssertionError("Mip pixels differ at level " + level);
                }
            }
        } finally {
            closeGenerated(vanilla);
            closeGenerated(webgpu);
        }
    }

    private static void closeGenerated(NativeImage[] images) {
        for (int level = 1; level < images.length; level++) images[level].close();
    }

    private static double medianMs(List<Long> samples) {
        long[] sorted = samples.stream().mapToLong(Long::longValue).sorted().toArray();
        return (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2_000_000.0;
    }

    private static double percentile95Ms(List<Long> samples) {
        long[] sorted = samples.stream().mapToLong(Long::longValue).sorted().toArray();
        int index = (int) Math.ceil(sorted.length * 0.95) - 1;
        return sorted[index] / 1_000_000.0;
    }
}
