package dev.fe2o3.mixin;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fe2o3.Blaze3DBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Inject(method = "initRenderer", at = @At("RETURN"))
    private static void fe2o3$rendererInitialized(GpuDevice device, CallbackInfo callback) {
        Blaze3DBackend.rendererInitialized(device);
    }
}
