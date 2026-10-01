package com.faktocraft.client.render;

import com.faktocraft.common.util.SpriteUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.quarry.BlockEntityLandmark;
import com.faktocraft.common.block.impl.quarry.BlockLandmark;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LandmarkRenderer implements BlockEntityRenderer<BlockEntityLandmark, LandmarkRenderer.State> {

  private static final Identifier BEAM_SPRITE = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "block/misc/landmark_beam");
  private static final int SCAN_INTERVAL_TICKS = 10;
  private static final int COLOR = 0xB45AC8FF;
  private static final float LOW = 7.25F / 16.0F;
  private static final float HIGH = 8.75F / 16.0F;

  public static class State extends BlockEntityRenderState {
    @Nullable
    TextureAtlasSprite sprite;
    float eastEnd = Float.NaN;
    float southEnd = Float.NaN;
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityLandmark landmark, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(landmark, state, partialTick, cameraPos, breakProgress);
    state.sprite = null;
    state.eastEnd = Float.NaN;
    state.southEnd = Float.NaN;
    Level level = landmark.getLevel();
    if (level == null) {
      return;
    }
    long now = level.getGameTime();
    BlockPos pos = landmark.getBlockPos();
    if (now >= landmark.nextScanTime) {
      landmark.eastPartner = BlockLandmark.findPartner(level, pos, Direction.EAST);
      landmark.southPartner = BlockLandmark.findPartner(level, pos, Direction.SOUTH);
      landmark.nextScanTime = now + SCAN_INTERVAL_TICKS;
    }
    if (landmark.eastPartner == null && landmark.southPartner == null) {
      return;
    }
    state.sprite = FluidSprites.block(BEAM_SPRITE);
    if (landmark.eastPartner != null) {
      state.eastEnd = landmark.eastPartner.getX() - pos.getX() + 0.5F;
    }
    if (landmark.southPartner != null) {
      state.southEnd = landmark.southPartner.getZ() - pos.getZ() + 0.5F;
    }
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    TextureAtlasSprite sprite = state.sprite;
    if (sprite == null) {
      return;
    }
    float eastEnd = state.eastEnd;
    float southEnd = state.southEnd;
    collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(SpriteUtil.blockAtlas()),
        (pose, vc) -> {
          if (!Float.isNaN(eastEnd)) {
            CuboidRenderer.drawBox(pose, vc, sprite, COLOR, LightCoordsUtil.FULL_BRIGHT,
                0.5F, LOW, LOW, eastEnd, HIGH, HIGH);
          }
          if (!Float.isNaN(southEnd)) {
            CuboidRenderer.drawBox(pose, vc, sprite, COLOR, LightCoordsUtil.FULL_BRIGHT,
                LOW, LOW, 0.5F, HIGH, HIGH, southEnd);
          }
        });
  }

  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }

  @Override
  public int getViewDistance() {
    return BlockLandmark.MAX_SPAN * 2;
  }

  @Override
  public AABB getRenderBoundingBox(BlockEntityLandmark landmark) {
    return landmark.getRenderBoundingBox();
  }
}
