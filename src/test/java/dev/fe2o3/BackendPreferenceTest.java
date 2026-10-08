package dev.fe2o3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackendPreferenceTest {
    @Test
    void mapsBlaze3dBackendNames() {
        assertEquals("gl", BackendPreference.fromBlaze3D("OpenGL"));
        assertEquals("gl", BackendPreference.fromBlaze3D("OpenGL 4.6"));
        assertEquals("vulkan", BackendPreference.fromBlaze3D("Vulkan"));
        assertEquals("auto", BackendPreference.fromBlaze3D(null));
        assertEquals("auto", BackendPreference.fromBlaze3D("unknown"));
    }
}
