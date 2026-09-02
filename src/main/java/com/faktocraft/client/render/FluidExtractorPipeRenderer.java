package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class FluidExtractorPipeRenderer extends FluidPipeRenderer {

  @Override
  public void render(BlockEntityFluidPipe pipe, float partialTick, PoseStack poseStack,
      MultiBufferSource buffer, int packedLight, int packedOverlay) {
    super.render(pipe, partialTick, poseStack, buffer, packedLight, packedOverlay);
    ExtractorSocketRenderer.render(pipe, poseStack, buffer, packedOverlay);
    ExtractorRingRenderer.render(pipe, poseStack, buffer, packedOverlay);
  }
}
