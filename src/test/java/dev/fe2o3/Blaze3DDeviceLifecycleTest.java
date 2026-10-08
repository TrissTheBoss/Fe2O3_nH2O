package dev.fe2o3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

final class Blaze3DDeviceLifecycleTest {
    @Test
    void sameDeviceDoesNotAdvanceGeneration() {
        var state = new Blaze3DDeviceLifecycle.DeviceEpoch<Object>();
        Object device = new Object();

        assertEquals(1, state.activate(device));
        assertEquals(1, state.activate(device));
        assertSame(device, state.active());
    }

    @Test
    void replacementAdvancesGenerationAndDetachClearsDevice() {
        var state = new Blaze3DDeviceLifecycle.DeviceEpoch<Object>();
        Object first = new Object();
        Object second = new Object();

        assertEquals(1, state.activate(first));
        assertSame(first, state.detach());
        assertNull(state.active());
        assertEquals(2, state.activate(second));
        assertSame(second, state.active());
    }

    @Test
    void detachBeforeFailedInitializationDoesNotChangeGeneration() {
        var state = new Blaze3DDeviceLifecycle.DeviceEpoch<Object>();
        Object first = new Object();

        assertEquals(1, state.activate(first));
        assertSame(first, state.detach());
        assertNull(state.active());
        assertEquals(1, state.generation());
    }
}
