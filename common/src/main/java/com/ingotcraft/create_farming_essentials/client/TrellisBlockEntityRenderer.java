package com.ingotcraft.create_farming_essentials.client;

import com.ingotcraft.create_farming_essentials.block.TrellisBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;

/** Draws the planted crop's block model for its current state inside the trellis. */
public class TrellisBlockEntityRenderer implements BlockEntityRenderer<TrellisBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public TrellisBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(TrellisBlockEntity trellis, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        trellis.getCrop().ifPresent(crop ->
                blockRenderer.renderSingleBlock(
                        crop.displayState(trellis.getAge(), trellis.getFruitDirection()),
                        poseStack, buffer, packedLight, packedOverlay));
    }
}
