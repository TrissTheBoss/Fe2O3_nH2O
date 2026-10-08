package dev.fe2o3;

import java.util.Locale;

/** Maps Blaze3D's reported backend name to a best-effort wgpu preference. */
public final class BackendPreference {
    private BackendPreference() { }

    public static String fromBlaze3D(String backendName) {
        if (backendName == null) return "auto";
        String normalized = backendName.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("gl") || normalized.contains("opengl")) return "gl";
        if (normalized.equals("vulkan") || normalized.startsWith("vulkan ")) return "vulkan";
        return "auto";
    }
}
