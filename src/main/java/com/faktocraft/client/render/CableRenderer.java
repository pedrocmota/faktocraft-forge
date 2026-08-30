package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.Vec3;

public class CableRenderer implements BlockEntityRenderer<BlockEntityCable> {

  @Override
  public boolean shouldRender(BlockEntityCable cable, Vec3 cameraPos) {
    return cable.supportDirection() != null
        && Vec3.atCenterOf(cable.getBlockPos()).closerThan(cameraPos, getViewDistance());
  }

  @Override
  public void render(BlockEntityCable cable, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
    PipeSupportRenderer.render(cable, poseStack, buffer, packedLight, packedOverlay);
  }
}
