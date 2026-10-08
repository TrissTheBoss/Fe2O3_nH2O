package dev.fe2o3;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Minecraft-specific adapter. Returning null always means execute vanilla unchanged. */
public final class Mipmaps {
    private static final Logger LOGGER = LoggerFactory.getLogger("Fe2O3_nH2O");
    private static final int MIN_PIXELS = Math.clamp(Integer.getInteger("fe2o3.minPixels", 4096), 4, 2048 * 2048);
    private static boolean ready;
    private static boolean disabled = !Boolean.parseBoolean(System.getProperty("fe2o3.enabled", "true"));
    private static long completed;

    private Mipmaps() { }

    public static int[] colorTables() {
        int[] tables = new int[1280];
        for (int i = 0; i < 256; i++) tables[i] = Math.round(ARGB.srgbToLinearChannel(i) * 1023.0f);
        // Stay strictly inside each integer bin to recover the game's table exactly.
        for (int i = 0; i < 1024; i++) tables[256 + i] = ARGB.linearToSrgbChannel((i + 0.25f) / 1023.0f);
        return tables;
    }

    public static synchronized boolean initialize() {
        if (disabled) return false;
        if (ready) return true;
        try {
            NativeLoader.load();
            NativeBridge.initialize(colorTables(), Blaze3DBackend.wgpuPreference());
            Runtime.getRuntime().addShutdownHook(new Thread(NativeBridge::shutdown, "Fe2O3 shutdown"));
            ready = true;
            LOGGER.info("Rust/WebGPU mipmap stage initialized; minimum {} source pixels; backend preference {}",
                    MIN_PIXELS, Blaze3DBackend.wgpuPreference());
            return true;
        } catch (Exception | LinkageError error) {
            disable(error);
            return false;
        }
    }

    private static void disable(Throwable error) {
        disabled = true;
        LOGGER.warn("WebGPU mipmaps disabled for this session; using vanilla", error);
    }

    public static synchronized long completed() { return completed; }

    public static synchronized NativeImage[] tryGenerate(NativeImage[] input, int levels,
            MipmapStrategy strategy, Transparency transparency) {
        if (disabled || input == null || input.length != 1 || input[0] == null || levels < 1 || levels > 11) return null;
        boolean mean = strategy == MipmapStrategy.MEAN
                || (strategy == MipmapStrategy.AUTO && transparency != null && !transparency.hasTransparent());
        if (!mean) return null;
        NativeImage base = input[0];
        int width = base.getWidth();
        int height = base.getHeight();
        if (base.format() != NativeImage.Format.RGBA || width > 2048 || height > 2048
                || (long) width * height < MIN_PIXELS || (width >> levels) < 1 || (height >> levels) < 1) return null;
        if (!initialize()) return null;
        NativeImage[] result = new NativeImage[levels + 1];
        result[0] = base;
        boolean published = false;
        try {
            int[] pixels = NativeBridge.generate(base.getPixels(), width, height, levels);
            int expected = 0;
            for (int level = 1; level <= levels; level++) expected += (width >> level) * (height >> level);
            if (pixels == null || pixels.length != expected) throw new IllegalStateException("Invalid native result length");
            int cursor = 0;
            for (int level = 1; level <= levels; level++) {
                NativeImage image = new NativeImage(width >> level, height >> level, false);
                result[level] = image;
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) image.setPixel(x, y, pixels[cursor++]);
                }
            }
            completed++;
            if (completed == 1) LOGGER.info("WebGPU generated first Minecraft mip chain ({}x{}, {} levels)", width, height, levels);
            published = true;
            return result;
        } catch (RuntimeException | LinkageError error) {
            disable(error);
            return null;
        } finally {
            // Never close or mutate an image owned by Minecraft on the fallback path.
            if (!published) for (int i = 1; i < result.length; i++) if (result[i] != null) result[i].close();
        }
    }
}

