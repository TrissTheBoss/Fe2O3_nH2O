package dev.fe2o3.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import dev.fe2o3.Mipmaps;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MipmapGenerator.class)
public abstract class MipmapGeneratorMixin {
    @Inject(method = "generateMipLevels", at = @At("HEAD"), cancellable = true)
    private static void fe2o3$generate(Identifier id, NativeImage[] input, int levels,
            MipmapStrategy strategy, float alphaBias, Transparency transparency,
            CallbackInfoReturnable<NativeImage[]> callback) {
        NativeImage[] result = Mipmaps.tryGenerate(input, levels, strategy, transparency);
        if (result != null) callback.setReturnValue(result);
    }
}
