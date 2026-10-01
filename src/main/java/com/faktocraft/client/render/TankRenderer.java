package com.faktocraft.client.render;

import com.faktocraft.common.util.SpriteUtil;
import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TankRenderer implements BlockEntityRenderer<BlockEntityTank, TankRenderer.State> {

  private static final float X0 = 2.5f / 16.0f;
  private static final float X1 = 13.5f / 16.0f;
  private static final float Y0 = 0.5f / 16.0f;
  private static final float Y_MAX = 15.5f / 16.0f;

  public static class State extends BlockEntityRenderState {
    @Nullable
    TextureAtlasSprite sprite;
    int color;
    float y0;
    float top;
    boolean seamDown;
    boolean seamUp;
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityTank tank, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(tank, state, partialTick, cameraPos, breakProgress);
    state.sprite = null;
    if (tank.tank.isEmpty()) {
      return;
    }
    float fillFraction = (float) tank.tank.getFluidAmount() / tank.tank.getCapacityMb();
    if (fillFraction <= 0.001f) {
      return;
    }
    Fluid fluid = tank.tank.getFluid();
    TextureAtlasSprite sprite = FluidSprites.still(fluid);
    if (sprite == null) {
      return;
    }
    state.color = FluidSprites.tint(fluid) | 0xFF000000;

    boolean seamDown = false;
    boolean seamUp = false;
    var level = tank.getLevel();
    if (level != null) {
      seamDown = level.getBlockEntity(tank.getBlockPos().below()) instanceof BlockEntityTank below
          && !below.tank.isEmpty() && below.tank.getFluid().isSame(fluid)
          && below.tank.getFluidAmount() >= below.tank.getCapacityMb();
      seamUp = fillFraction >= 0.999f
          && level.getBlockEntity(tank.getBlockPos().above()) instanceof BlockEntityTank above
          && !above.tank.isEmpty() && above.tank.getFluid().isSame(fluid);
    }
    float y0 = seamDown ? 0.0f : Y0;
    float topLimit = seamUp ? 1.0f : Y_MAX;
    state.y0 = y0;
    state.top = y0 + (topLimit - y0) * Math.min(1.0f, fillFraction);
    state.seamDown = seamDown;
    state.seamUp = seamUp;
    state.sprite = sprite;
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    TextureAtlasSprite sprite = state.sprite;
    if (sprite == null) {
      return;
    }
    int light = state.lightCoords;
    collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(SpriteUtil.blockAtlas()),
        (pose, vc) -> CuboidRenderer.drawBox(pose, vc, sprite, state.color, light, X0, state.y0, X0, X1, state.top,
            X1, !state.seamDown, !state.seamUp));
  }
}
