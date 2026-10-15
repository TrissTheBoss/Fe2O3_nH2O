package dev.fe2o3.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    private static void fe2o3$seedOutlineFixture(LevelRenderState levelRenderState) {
        if (!Boolean.getBoolean("fe2o3.testOutlineFixture") || levelRenderState.blockOutlineRenderState != null) return;
        Minecraft client = Minecraft.getInstance();
        BlockPos target = new BlockPos(0, 100, 0);
        if (client.level == null) return;
        BlockState blockState = client.level.getBlockState(target);
        if (!blockState.is(Blocks.STONE)) return;

        BlockStateModel model = client.getModelManager().getBlockStateModelSet().get(blockState);
        boolean translucent = model.hasMaterialFlag(1);
        boolean highContrast = client.options.highContrastBlockOutline().get();
        VoxelShape shape = blockState.getShape(client.level, target, CollisionContext.of(client.player));
        levelRenderState.blockOutlineRenderState =
                new BlockOutlineRenderState(target, translucent, highContrast, shape);
    }

    @Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
    private void fe2o3$submitBlockOutline(PoseStack poseStack, SubmitNodeCollector collector,
                                          LevelRenderState levelRenderState, CallbackInfo callback) {
        fe2o3$seedOutlineFixture(levelRenderState);
        if (Blaze3DBlockOutlinePass.trySubmit(poseStack, collector, levelRenderState)) {
            callback.cancel();
        }
    }
}
