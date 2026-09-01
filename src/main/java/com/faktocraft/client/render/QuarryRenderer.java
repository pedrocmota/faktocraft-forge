package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.quarry.BlockEntityQuarry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class QuarryRenderer implements BlockEntityRenderer<BlockEntityQuarry> {

  private static final ResourceLocation FRAME_SPRITE = new ResourceLocation(Faktocraft.MODID,
      "block/misc/quarry_frame");
  private static final float CHASE_SPEED = 0.3F;
  private static final float BEAM_HALF = 0.125F;
  private static final int WHITE = 0xFFFFFFFF;
  private static final int DARK = 0xFF6E6E6E;

  private static final int AXIS_X = 0;
  private static final int AXIS_Y = 1;
  private static final int AXIS_Z = 2;

  @Override
  public void render(BlockEntityQuarry quarry, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
    Level level = quarry.getLevel();
    if (level == null || !quarry.hasArea() || quarry.stage() < BlockEntityQuarry.STAGE_FRAME) {
      return;
    }

    double now = level.getGameTime() + partialTick;
    if (Double.isNaN(quarry.clientHeadLastTime)) {
      quarry.clientHeadX = quarry.headX;
      quarry.clientHeadY = quarry.headY;
      quarry.clientHeadZ = quarry.headZ;
    } else {
      float elapsed = Math.min(3.0F, (float) (now - quarry.clientHeadLastTime));
      float step = CHASE_SPEED * Math.max(0.0F, elapsed);
      quarry.clientHeadX = chase(quarry.clientHeadX, quarry.headX, step);
      quarry.clientHeadY = chase(quarry.clientHeadY, quarry.headY, step);
      quarry.clientHeadZ = chase(quarry.clientHeadZ, quarry.headZ, step);
    }
    quarry.clientHeadLastTime = now;

    TextureAtlasSprite sprite = Minecraft.getInstance()
        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FRAME_SPRITE);
    if (sprite == null) {
      return;
    }
    VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
    PoseStack.Pose pose = poseStack.last();

    BlockPos origin = quarry.getBlockPos();
    float headX = quarry.clientHeadX - origin.getX();
    float headY = quarry.clientHeadY - origin.getY();
    float headZ = quarry.clientHeadZ - origin.getZ();
    float gantryY = BlockEntityQuarry.FRAME_HEIGHT_ABOVE;
    float innerX0 = quarry.areaMinX() + 1 - origin.getX();
    float innerX1 = quarry.areaMaxX() - origin.getX();
    float innerZ0 = quarry.areaMinZ() + 1 - origin.getZ();
    float innerZ1 = quarry.areaMaxZ() - origin.getZ();

    BlockPos headPos = BlockPos.containing(quarry.clientHeadX, quarry.clientHeadY + 0.5, quarry.clientHeadZ);
    int light = LevelRenderer.getLightColor(level,
        headPos.getY() >= level.getMaxBuildHeight() ? headPos.below() : headPos);
    int gantryLight = LevelRenderer.getLightColor(level,
        new BlockPos(headPos.getX(), origin.getY() + BlockEntityQuarry.FRAME_HEIGHT_ABOVE, headPos.getZ()));

    float clampedX = Mth.clamp(headX, innerX0 + BEAM_HALF, innerX1 - BEAM_HALF);
    float clampedZ = Mth.clamp(headZ, innerZ0 + BEAM_HALF, innerZ1 - BEAM_HALF);

    float railY0 = gantryY + 5.5F / 16.0F;
    float railY1 = gantryY + 10.5F / 16.0F;

    beam(pose, vc, sprite, WHITE, gantryLight, AXIS_X,
        innerX0 - 0.5F, innerX1 + 0.5F, railY0, railY1, clampedZ - BEAM_HALF, clampedZ + BEAM_HALF);
    beam(pose, vc, sprite, WHITE, gantryLight, AXIS_Z,
        innerZ0 - 0.5F, innerZ1 + 0.5F, clampedX - BEAM_HALF, clampedX + BEAM_HALF, railY0, railY1);

    movingBox(pose, vc, sprite, WHITE, gantryLight,
        clampedX - 0.3125F, gantryY + 3 / 16.0F, clampedZ - 0.3125F,
        clampedX + 0.3125F, gantryY + 13 / 16.0F, clampedZ + 0.3125F);

    if (headY < railY0) {
      int stringX = Mth.floor(origin.getX() + clampedX);
      int stringZ = Mth.floor(origin.getZ() + clampedZ);
      beam(pose, vc, sprite, WHITE,
          yRel -> LevelRenderer.getLightColor(level,
              new BlockPos(stringX, Mth.clamp(origin.getY() + yRel, level.getMinBuildHeight(),
                  level.getMaxBuildHeight() - 1), stringZ)),
          AXIS_Y,
          headY - 0.15F, railY0, clampedX - BEAM_HALF, clampedX + BEAM_HALF,
          clampedZ - BEAM_HALF, clampedZ + BEAM_HALF);
      movingBox(pose, vc, sprite, DARK, light,
          clampedX - 0.22F, headY - 0.3F, clampedZ - 0.22F,
          clampedX + 0.22F, headY, clampedZ + 0.22F);
    }
  }

  private static void beam(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sprite, int color,
      int light, int axis, float a0, float a1, float b0, float b1, float c0, float c1) {
    beam(pose, vc, sprite, color, yRel -> light, axis, a0, a1, b0, b1, c0, c1);
  }

  private static void beam(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sprite, int color,
      java.util.function.IntUnaryOperator lightFn, int axis, float a0, float a1, float b0, float b1,
      float c0, float c1) {
    float vb0 = sprite.getV(fracUv(b0));
    float vb1 = sprite.getV(fracUv(b1));
    float vc0 = sprite.getV(fracUv(c0));
    float vc1 = sprite.getV(fracUv(c1));

    float s = a0;
    while (s < a1) {
      float n = (float) Math.floor(s);
      float e = Math.min(a1, n + 1 > s ? n + 1 : s + 1);
      float u0 = sprite.getU((s - n) * 16.0F);
      float u1 = sprite.getU((e - n) * 16.0F);
      int light = lightFn.applyAsInt((int) n);

      vert(pose, vc, axis, s, b0, c0, u0, vc0, color, light, 0, -1, 0);
      vert(pose, vc, axis, e, b0, c0, u1, vc0, color, light, 0, -1, 0);
      vert(pose, vc, axis, e, b0, c1, u1, vc1, color, light, 0, -1, 0);
      vert(pose, vc, axis, s, b0, c1, u0, vc1, color, light, 0, -1, 0);

      vert(pose, vc, axis, s, b1, c0, u0, vc0, color, light, 0, 1, 0);
      vert(pose, vc, axis, e, b1, c0, u1, vc0, color, light, 0, 1, 0);
      vert(pose, vc, axis, e, b1, c1, u1, vc1, color, light, 0, 1, 0);
      vert(pose, vc, axis, s, b1, c1, u0, vc1, color, light, 0, 1, 0);

      vert(pose, vc, axis, s, b0, c0, u0, vb0, color, light, 0, 0, -1);
      vert(pose, vc, axis, e, b0, c0, u1, vb0, color, light, 0, 0, -1);
      vert(pose, vc, axis, e, b1, c0, u1, vb1, color, light, 0, 0, -1);
      vert(pose, vc, axis, s, b1, c0, u0, vb1, color, light, 0, 0, -1);

      vert(pose, vc, axis, s, b0, c1, u0, vb0, color, light, 0, 0, 1);
      vert(pose, vc, axis, e, b0, c1, u1, vb0, color, light, 0, 0, 1);
      vert(pose, vc, axis, e, b1, c1, u1, vb1, color, light, 0, 0, 1);
      vert(pose, vc, axis, s, b1, c1, u0, vb1, color, light, 0, 0, 1);

      s = e;
    }

    float ub0 = sprite.getU(fracUv(b0));
    float ub1 = sprite.getU(fracUv(b1));
    int lightA0 = lightFn.applyAsInt(Mth.floor(a0));
    int lightA1 = lightFn.applyAsInt(Mth.floor(a1 - 1.0E-4F));
    vert(pose, vc, axis, a0, b0, c0, ub0, vc0, color, lightA0, -1, 0, 0);
    vert(pose, vc, axis, a0, b1, c0, ub1, vc0, color, lightA0, -1, 0, 0);
    vert(pose, vc, axis, a0, b1, c1, ub1, vc1, color, lightA0, -1, 0, 0);
    vert(pose, vc, axis, a0, b0, c1, ub0, vc1, color, lightA0, -1, 0, 0);

    vert(pose, vc, axis, a1, b0, c0, ub0, vc0, color, lightA1, 1, 0, 0);
    vert(pose, vc, axis, a1, b1, c0, ub1, vc0, color, lightA1, 1, 0, 0);
    vert(pose, vc, axis, a1, b1, c1, ub1, vc1, color, lightA1, 1, 0, 0);
    vert(pose, vc, axis, a1, b0, c1, ub0, vc1, color, lightA1, 1, 0, 0);
  }

  private static void vert(PoseStack.Pose pose, VertexConsumer vc, int axis, float a, float b, float c,
      float u, float v, int color, int light, float na, float nb, float nc) {
    float x;
    float y;
    float z;
    float nx;
    float ny;
    float nz;
    switch (axis) {
      case AXIS_Y -> {
        x = b;
        y = a;
        z = c;
        nx = nb;
        ny = na;
        nz = nc;
      }
      case AXIS_Z -> {
        x = b;
        y = c;
        z = a;
        nx = nb;
        ny = nc;
        nz = na;
      }
      default -> {
        x = a;
        y = b;
        z = c;
        nx = na;
        ny = nb;
        nz = nc;
      }
    }
    CuboidRenderer.vertex(pose, vc, x, y, z, u, v, color, light, nx, ny, nz);
  }

  private static float fracUv(float coord) {
    return (coord - (float) Math.floor(coord)) * 16.0F;
  }

  private static void movingBox(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sprite, int color,
      int light, float x0, float y0, float z0, float x1, float y1, float z1) {
    float sx = (16.0F - (x1 - x0) * 16.0F) / 2.0F;
    float sy = (16.0F - (y1 - y0) * 16.0F) / 2.0F;
    float sz = (16.0F - (z1 - z0) * 16.0F) / 2.0F;
    float ux0 = sprite.getU(sx);
    float ux1 = sprite.getU(sx + (x1 - x0) * 16.0F);
    float uz0 = sprite.getU(sz);
    float uz1 = sprite.getU(sz + (z1 - z0) * 16.0F);
    float vz0 = sprite.getV(sz);
    float vz1 = sprite.getV(sz + (z1 - z0) * 16.0F);
    float vyTop = sprite.getV(sy);
    float vyBottom = sprite.getV(sy + (y1 - y0) * 16.0F);

    CuboidRenderer.vertex(pose, vc, x0, y0, z0, ux0, vz0, color, light, 0, -1, 0);
    CuboidRenderer.vertex(pose, vc, x1, y0, z0, ux1, vz0, color, light, 0, -1, 0);
    CuboidRenderer.vertex(pose, vc, x1, y0, z1, ux1, vz1, color, light, 0, -1, 0);
    CuboidRenderer.vertex(pose, vc, x0, y0, z1, ux0, vz1, color, light, 0, -1, 0);

    CuboidRenderer.vertex(pose, vc, x0, y1, z1, ux0, vz1, color, light, 0, 1, 0);
    CuboidRenderer.vertex(pose, vc, x1, y1, z1, ux1, vz1, color, light, 0, 1, 0);
    CuboidRenderer.vertex(pose, vc, x1, y1, z0, ux1, vz0, color, light, 0, 1, 0);
    CuboidRenderer.vertex(pose, vc, x0, y1, z0, ux0, vz0, color, light, 0, 1, 0);

    CuboidRenderer.vertex(pose, vc, x1, y0, z0, ux1, vyBottom, color, light, 0, 0, -1);
    CuboidRenderer.vertex(pose, vc, x1, y1, z0, ux1, vyTop, color, light, 0, 0, -1);
    CuboidRenderer.vertex(pose, vc, x0, y1, z0, ux0, vyTop, color, light, 0, 0, -1);
    CuboidRenderer.vertex(pose, vc, x0, y0, z0, ux0, vyBottom, color, light, 0, 0, -1);

    CuboidRenderer.vertex(pose, vc, x0, y0, z1, ux0, vyBottom, color, light, 0, 0, 1);
    CuboidRenderer.vertex(pose, vc, x0, y1, z1, ux0, vyTop, color, light, 0, 0, 1);
    CuboidRenderer.vertex(pose, vc, x1, y1, z1, ux1, vyTop, color, light, 0, 0, 1);
    CuboidRenderer.vertex(pose, vc, x1, y0, z1, ux1, vyBottom, color, light, 0, 0, 1);

    CuboidRenderer.vertex(pose, vc, x0, y0, z0, uz0, vyBottom, color, light, -1, 0, 0);
    CuboidRenderer.vertex(pose, vc, x0, y1, z0, uz0, vyTop, color, light, -1, 0, 0);
    CuboidRenderer.vertex(pose, vc, x0, y1, z1, uz1, vyTop, color, light, -1, 0, 0);
    CuboidRenderer.vertex(pose, vc, x0, y0, z1, uz1, vyBottom, color, light, -1, 0, 0);

    CuboidRenderer.vertex(pose, vc, x1, y0, z1, uz1, vyBottom, color, light, 1, 0, 0);
    CuboidRenderer.vertex(pose, vc, x1, y1, z1, uz1, vyTop, color, light, 1, 0, 0);
    CuboidRenderer.vertex(pose, vc, x1, y1, z0, uz0, vyTop, color, light, 1, 0, 0);
    CuboidRenderer.vertex(pose, vc, x1, y0, z0, uz0, vyBottom, color, light, 1, 0, 0);
  }

  private static float chase(float current, float target, float step) {
    return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
  }

  @Override
  public boolean shouldRenderOffScreen(BlockEntityQuarry quarry) {
    return true;
  }

  @Override
  public boolean shouldRender(BlockEntityQuarry quarry, Vec3 cameraPos) {
    double d = getViewDistance() * 4;
    return quarry.getRenderBoundingBox().distanceToSqr(cameraPos) < d * d;
  }
}
