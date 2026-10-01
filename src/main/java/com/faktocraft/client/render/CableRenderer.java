package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CableRenderer implements BlockEntityRenderer<BlockEntityCable, CableRenderer.State> {

  public static class State extends BlockEntityRenderState {
    final PipeSupportRenderer.Data support = new PipeSupportRenderer.Data();
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public boolean shouldRender(BlockEntityCable cable, Vec3 cameraPos) {
    return cable.supportDirection() != null
        && Vec3.atCenterOf(cable.getBlockPos()).closerThan(cameraPos, getViewDistance());
  }

  @Override
  public void extractRenderState(BlockEntityCable cable, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(cable, state, partialTick, cameraPos, breakProgress);
    PipeSupportRenderer.extract(cable, state.support);
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    PipeSupportRenderer.submit(state.support, poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
  }
}
