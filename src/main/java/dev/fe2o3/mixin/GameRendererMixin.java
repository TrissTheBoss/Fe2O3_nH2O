package dev.fe2o3.mixin;

import dev.fe2o3.Blaze3DDeviceLifecycle;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "close", at = @At("HEAD"))
    private void fe2o3$rendererClosing(CallbackInfo callback) {
        Blaze3DDeviceLifecycle.rendererClosing();
    }
}
