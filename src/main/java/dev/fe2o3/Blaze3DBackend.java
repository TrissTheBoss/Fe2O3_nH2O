package dev.fe2o3;

import com.mojang.blaze3d.systems.GpuDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Captures Blaze3D's chosen device after Minecraft initializes its renderer. */
public final class Blaze3DBackend {
    private static final Logger LOGGER = LoggerFactory.getLogger("Fe2O3_nH2O");
    private static volatile String wgpuPreference = "auto";

    private Blaze3DBackend() { }

    public static void rendererInitialized(GpuDevice device) {
        String backendName = device.getDeviceInfo().backendName();
        wgpuPreference = BackendPreference.fromBlaze3D(backendName);
        LOGGER.info("Blaze3D selected backend '{}'; Fe2O3 wgpu preference is '{}'",
                backendName, wgpuPreference);
    }

    public static String wgpuPreference() {
        return wgpuPreference;
    }
}
