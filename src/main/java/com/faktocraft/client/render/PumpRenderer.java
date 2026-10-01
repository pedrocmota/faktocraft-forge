package com.faktocraft.client.render;

import com.faktocraft.common.util.SpriteUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.pipe.BlockEntityPump;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PumpRenderer implements BlockEntityRenderer<BlockEntityPump, PumpRenderer.State> {
  private static final float R0 = 6.5f / 16.0f;
  private static final float R1 = 9.5f / 16.0f;
  private static final float CHASE_SPEED = 0.2f;
  private static final Identifier TUBE_SPRITE = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "block/pipe/pump_tube");

  public static class State extends BlockEntityRenderState {
    @Nullable
    TextureAtlasSprite sprite;
    float depth;
    int[] segmentLight = new int[0];
    int capLight;
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityPump pump, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(pump, state, partialTick, cameraPos, breakProgress);
    state.sprite = null;
    Level level = pump.getLevel();
    if (level == null) {
      return;
    }

    double now = level.getGameTime() + partialTick;
    if (Double.isNaN(pump.clientTubeLastTime)) {
      pump.clientTubeDepth = pump.tubeDepth;
    } else {
      float elapsed = Math.min(3.0f, (float) (now - pump.clientTubeLastTime));
      float step = CHASE_SPEED * Math.max(0.0f, elapsed);
      pump.clientTubeDepth = pump.clientTubeDepth < pump.tubeDepth
          ? Math.min(pump.tubeDepth, pump.clientTubeDepth + step)
          : Math.max(pump.tubeDepth, pump.clientTubeDepth - step);
    }
    pump.clientTubeLastTime = now;

    float depth = pump.clientTubeDepth;
    if (depth <= 0.02f) {
      return;
    }
    state.sprite = FluidSprites.block(TUBE_SPRITE);
    state.depth = depth;
    int fullSegments = Mth.floor(depth);
    if (state.segmentLight.length < fullSegments + 1) {
      state.segmentLight = new int[fullSegments + 1];
    }
    for (int i = 0; i <= fullSegments; i++) {
      state.segmentLight[i] = LightCoordsUtil.getLightCoords(level, pump.getBlockPos().below(i + 1));
    }
    state.capLight = LightCoordsUtil.getLightCoords(level, pump.getBlockPos().below(fullSegments + 1));
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    TextureAtlasSprite sprite = state.sprite;
    if (sprite == null) {
      return;
    }
    float depth = state.depth;
    int[] segmentLight = state.segmentLight;
    int capLight = state.capLight;
    collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(SpriteUtil.blockAtlas()),
        (pose, vc) -> draw(pose, vc, sprite, depth, segmentLight, capLight));
  }

  private static void draw(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sprite, float depth,
      int[] segmentLight, int capLight) {
    int white = 0xFFFFFFFF;
    int fullSegments = Mth.floor(depth);
    for (int i = 0; i <= fullSegments; i++) {
      float yTop = -i;
      float yBottom = Math.max(-depth, yTop - 1.0f);
      float height = yTop - yBottom;
      if (height <= 0.001f) {
        continue;
      }
      int light = segmentLight[i];
      float u0 = sprite.getU0();
      float u1 = SpriteUtil.getU(sprite, 16.0f * height);
      float v0 = sprite.getV0();
      float vMid = SpriteUtil.getV(sprite, 8.0f);
      sideZ(pose, vc, R0, yTop, yBottom, u0, u1, v0, vMid, white, light, -1);
      sideZ(pose, vc, R1, yTop, yBottom, u0, u1, v0, vMid, white, light, 1);
      sideX(pose, vc, R0, yTop, yBottom, u0, u1, v0, vMid, white, light, -1);
      sideX(pose, vc, R1, yTop, yBottom, u0, u1, v0, vMid, white, light, 1);
    }

    float yCap = -depth;
    float cu0 = sprite.getU0();
    float cu1 = SpriteUtil.getU(sprite, 8.0f);
    float cv0 = SpriteUtil.getV(sprite, 8.0f);
    float cv1 = sprite.getV1();
    CuboidRenderer.vertex(pose, vc, R0, yCap, R0, cu0, cv0, white, capLight, 0, -1, 0);
    CuboidRenderer.vertex(pose, vc, R1, yCap, R0, cu1, cv0, white, capLight, 0, -1, 0);
    CuboidRenderer.vertex(pose, vc, R1, yCap, R1, cu1, cv1, white, capLight, 0, -1, 0);
    CuboidRenderer.vertex(pose, vc, R0, yCap, R1, cu0, cv1, white, capLight, 0, -1, 0);
  }

  private static void sideZ(PoseStack.Pose pose, VertexConsumer vc, float z, float yTop, float yBottom,
      float u0, float u1, float v0, float v1, int color, int light, int nz) {
    CuboidRenderer.vertex(pose, vc, R0, yBottom, z, u1, v0, color, light, 0, 0, nz);
    CuboidRenderer.vertex(pose, vc, R0, yTop, z, u0, v0, color, light, 0, 0, nz);
    CuboidRenderer.vertex(pose, vc, R1, yTop, z, u0, v1, color, light, 0, 0, nz);
    CuboidRenderer.vertex(pose, vc, R1, yBottom, z, u1, v1, color, light, 0, 0, nz);
  }

  private static void sideX(PoseStack.Pose pose, VertexConsumer vc, float x, float yTop, float yBottom,
      float u0, float u1, float v0, float v1, int color, int light, int nx) {
    CuboidRenderer.vertex(pose, vc, x, yBottom, R0, u1, v0, color, light, nx, 0, 0);
    CuboidRenderer.vertex(pose, vc, x, yTop, R0, u0, v0, color, light, nx, 0, 0);
    CuboidRenderer.vertex(pose, vc, x, yTop, R1, u0, v1, color, light, nx, 0, 0);
    CuboidRenderer.vertex(pose, vc, x, yBottom, R1, u1, v1, color, light, nx, 0, 0);
  }

  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }

  @Override
  public boolean shouldRender(BlockEntityPump pump, Vec3 cameraPos) {
    double d = getViewDistance();
    return pump.getRenderBoundingBox().distanceToSqr(cameraPos) < d * d;
  }

  @Override
  public AABB getRenderBoundingBox(BlockEntityPump pump) {
    return pump.getRenderBoundingBox();
  }
}
