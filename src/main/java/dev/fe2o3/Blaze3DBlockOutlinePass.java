package dev.fe2o3;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Experimental replacement for LevelRenderer's block-outline submission.
 * It consumes Minecraft's extracted state and leaves geometry/drawing to Blaze3D.
 */
public final class Blaze3DBlockOutlinePass {
    private static final AtomicLong SUBMITTED = new AtomicLong();

    private Blaze3DBlockOutlinePass() { }

    /**
     * Submits the outline and returns true when the vanilla submit method can be cancelled.
     * Disabled by default until vanilla image comparisons are complete.
     */
    public static boolean trySubmit(PoseStack poseStack, SubmitNodeCollector collector,
                                    net.minecraft.client.renderer.state.level.LevelRenderState levelState) {
        if (!Boolean.getBoolean("fe2o3.blaze3dOutline")
                || SharedConstants.DEBUG_SHAPES
                || Blaze3DDeviceLifecycle.generation() == 0) {
            return false;
        }

        BlockOutlineRenderState state = levelState.blockOutlineRenderState;
        if (state == null) return true;

        Vec3 cameraPos = levelState.cameraRenderState.pos;
        var pos = state.pos();
        poseStack.pushPose();
        try {
            poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
            if (state.highContrast()) {
                collector.submitShapeOutline(poseStack, state.shape(), RenderTypes.secondaryBlockOutline(),
                        -16777216, 7.0F, state.isTranslucent());
            }

            int color = state.highContrast() ? -11010079 : ARGB.black(102);
            float width = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
            collector.submitShapeOutline(poseStack, state.shape(), RenderTypes.lines(), color, width,
                    state.isTranslucent());
            SUBMITTED.incrementAndGet();
            return true;
        } finally {
            poseStack.popPose();
        }
    }

    /** Number of non-empty block outlines submitted through this experiment. */
    public static long submittedCount() {
        return SUBMITTED.get();
    }
}
