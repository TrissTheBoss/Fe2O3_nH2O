package dev.fe2o3;

import com.mojang.blaze3d.systems.GpuDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks the active Blaze3D device and provides a render-thread lifecycle for resources
 * owned by Fe2O3. This does not expose backend handles or share the independent wgpu device.
 */
public final class Blaze3DDeviceLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger("Fe2O3_nH2O");
    private static final DeviceEpoch<GpuDevice> DEVICE = new DeviceEpoch<>();
    private static final CopyOnWriteArrayList<Resource> RESOURCES = new CopyOnWriteArrayList<>();

    private Blaze3DDeviceLifecycle() { }

    /**
     * A resource that must be created for each active Blaze3D device. Implementations
     * should release all device-owned handles in {@link #onDeviceLost(GpuDevice)}.
     * Register and unregister on Minecraft's render thread.
     */
    public interface Resource {
        void onDeviceReady(GpuDevice device, long generation);
        void onDeviceLost(GpuDevice device);
    }

    /** Register a resource and initialize it immediately when a device is already active. */
    public static void register(Resource resource) {
        Objects.requireNonNull(resource, "resource");
        if (!RESOURCES.addIfAbsent(resource)) return;
        GpuDevice active = DEVICE.active();
        if (active != null) notifyReady(resource, active, DEVICE.generation());
    }

    /** Stop lifecycle notifications and release this resource if a device is active. */
    public static void unregister(Resource resource) {
        if (RESOURCES.remove(resource)) {
            GpuDevice active = DEVICE.active();
            if (active != null) notifyLost(resource, active);
        }
    }

    /** Called at the beginning of RenderSystem.initRenderer, while the old device is current. */
    public static void rendererWillInitialize(GpuDevice nextDevice) {
        GpuDevice previous = DEVICE.active();
        if (previous == null || previous == nextDevice) return;
        DEVICE.detach();
        for (Resource resource : RESOURCES) notifyLost(resource, previous);
    }

    /** Called after RenderSystem.initRenderer has installed its active device. */
    public static void rendererInitialized(GpuDevice device) {
        Objects.requireNonNull(device, "device");
        if (DEVICE.active() == device) return;

        GpuDevice previous = DEVICE.detach();
        if (previous != null) {
            for (Resource resource : RESOURCES) notifyLost(resource, previous);
        }
        long generation = DEVICE.activate(device);
        for (Resource resource : RESOURCES) notifyReady(resource, device, generation);
    }

    /** Called when GameRenderer closes, before Minecraft releases its renderer resources. */
    public static void rendererClosing() {
        GpuDevice previous = DEVICE.detach();
        if (previous != null) {
            for (Resource resource : RESOURCES) notifyLost(resource, previous);
        }
    }

    /** Current lifecycle generation, or zero before the first device is installed. */
    public static long generation() {
        return DEVICE.generation();
    }

    private static void notifyReady(Resource resource, GpuDevice device, long generation) {
        try {
            resource.onDeviceReady(device, generation);
        } catch (RuntimeException | LinkageError | AssertionError error) {
            notifyLost(resource, device);
            RESOURCES.remove(resource);
            LOGGER.warn("Removing a Fe2O3 Blaze3D resource after initialization failed", error);
        }
    }

    private static void notifyLost(Resource resource, GpuDevice device) {
        try {
            resource.onDeviceLost(device);
        } catch (RuntimeException | LinkageError | AssertionError error) {
            RESOURCES.remove(resource);
            LOGGER.warn("Removing a Fe2O3 Blaze3D resource after release failed", error);
        }
    }

    /** Small identity-based state holder, kept independent so lifecycle transitions can be unit tested. */
    static final class DeviceEpoch<T> {
        private volatile T active;
        private volatile long generation;

        T active() {
            return active;
        }

        long generation() {
            return generation;
        }

        long activate(T device) {
            if (active == device) return generation;
            active = Objects.requireNonNull(device, "device");
            return ++generation;
        }

        T detach() {
            T previous = active;
            active = null;
            return previous;
        }
    }
}
