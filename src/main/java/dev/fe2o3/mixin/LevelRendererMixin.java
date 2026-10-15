package dev.fe2o3.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import dev.fe2o3.Blaze3DBlockOutlinePass;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "extractBlockOutline", at = @At("HEAD"))
    private void fe2o3$seedOutlineFixture(Camera camera, LevelRenderState levelRenderState, CallbackInfo callback) {
        if (!Boolean.getBoolean("fe2o3.testOutlineFixture")) return;
        Minecraft client = Minecraft.getInstance();
        BlockPos target = new BlockPos(0, 100, 0);
        if (client.level != null && client.level.getBlockState(target).is(Blocks.STONE)) {
            client.hitResult = new BlockHitResult(new Vec3(0.5, 100.5, 0.5), Direction.UP, target, false);
        }
    }

    @Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
    private void fe2o3$submitBlockOutline(PoseStack poseStack, SubmitNodeCollector collector,
                                          LevelRenderState levelRenderState, CallbackInfo callback) {
        if (Blaze3DBlockOutlinePass.trySubmit(poseStack, collector, levelRenderState)) {
            callback.cancel();
        }
    }
}
