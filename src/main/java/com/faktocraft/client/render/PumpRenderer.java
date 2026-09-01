package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.pipe.BlockEntityPump;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PumpRenderer implements BlockEntityRenderer<BlockEntityPump> {

  private static final float R0 = 6.5f / 16.0f;
  private static final float R1 = 9.5f / 16.0f;
  private static final float CHASE_SPEED = 0.2f;
  private static final ResourceLocation TUBE_SPRITE = new ResourceLocation(Faktocraft.MODID, "block/pipe/pump_tube");

  @Override
  public void render(BlockEntityPump pump, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
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

    TextureAtlasSprite sprite = Minecraft.getInstance()
        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TUBE_SPRITE);
    if (sprite == null) {
      return;
    }
    VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
    PoseStack.Pose pose = poseStack.last();
    int white = 0xFFFFFFFF;

    int fullSegments = Mth.floor(depth);
    for (int i = 0; i <= fullSegments; i++) {
      float yTop = -i;
      float yBottom = Math.max(-depth, yTop - 1.0f);
      float height = yTop - yBottom;
      if (height <= 0.001f) {
        continue;
      }
      int light = LevelRenderer.getLightColor(level, pump.getBlockPos().below(i + 1));
      float u0 = sprite.getU0();
      float u1 = sprite.getU(16.0f * height);
      float v0 = sprite.getV0();
      float vMid = sprite.getV(8.0f);
      sideZ(pose, vc, R0, yTop, yBottom, u0, u1, v0, vMid, white, light, -1);
      sideZ(pose, vc, R1, yTop, yBottom, u0, u1, v0, vMid, white, light, 1);
      sideX(pose, vc, R0, yTop, yBottom, u0, u1, v0, vMid, white, light, -1);
      sideX(pose, vc, R1, yTop, yBottom, u0, u1, v0, vMid, white, light, 1);
    }

    float yCap = -depth;
    int capLight = LevelRenderer.getLightColor(level, pump.getBlockPos().below(fullSegments + 1));
    float cu0 = sprite.getU0();
    float cu1 = sprite.getU(8.0f);
    float cv0 = sprite.getV(8.0f);
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
  public boolean shouldRenderOffScreen(BlockEntityPump pump) {
    return true;
  }

  @Override
  public boolean shouldRender(BlockEntityPump pump, Vec3 cameraPos) {
    double d = getViewDistance();
    return pump.getRenderBoundingBox().distanceToSqr(cameraPos) < d * d;
  }
}
