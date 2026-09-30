package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;

/** Fallback used only when Flywheel's visualization is turned off (then no SprinklerVisual runs). */
public class SprinklerRenderer implements BlockEntityRenderer<SprinklerBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public SprinklerRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(SprinklerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null || VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        BakedModel model = SprinklerPartialModels.HEAD.get();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (be.isTop()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(be.getAngle(partialTick)));
        poseStack.translate(-0.5, -0.5, -0.5);
        dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()),
                be.getBlockState(), model, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
