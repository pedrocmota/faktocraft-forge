package com.faktocraft.client.render;

import com.faktocraft.common.util.SpriteUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class CuboidRenderer {

  private CuboidRenderer() {
  }

  public static void vertex(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float z,
      float u, float v, int color, int light, float nx, float ny, float nz) {
    vc.addVertex(pose, x, y, z)
        .setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24)
        .setUv(u, v)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(light)
        .setNormal(pose, nx, ny, nz);
  }

  public static void drawBox(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sprite,
      int color, int light, float x0, float y0, float z0, float x1, float y1, float z1) {
    drawBox(pose, vc, sprite, color, light, x0, y0, z0, x1, y1, z1, true, true);
  }

  public static void drawBox(PoseStack.Pose pose, VertexConsumer vc, TextureAtlasSprite sprite,
      int color, int light, float x0, float y0, float z0, float x1, float y1, float z1,
      boolean drawDown, boolean drawUp) {
    float ux0 = SpriteUtil.getU(sprite, clamp01(x0) * 16.0f);
    float ux1 = SpriteUtil.getU(sprite, clamp01(x1) * 16.0f);
    float uz0 = SpriteUtil.getU(sprite, clamp01(z0) * 16.0f);
    float uz1 = SpriteUtil.getU(sprite, clamp01(z1) * 16.0f);
    float vz0 = SpriteUtil.getV(sprite, clamp01(z0) * 16.0f);
    float vz1 = SpriteUtil.getV(sprite, clamp01(z1) * 16.0f);
    float vyTop = SpriteUtil.getV(sprite, clamp01(1 - y1) * 16.0f);
    float vyBottom = SpriteUtil.getV(sprite, clamp01(1 - y0) * 16.0f);

    if (drawDown) {
      vertex(pose, vc, x0, y0, z0, ux0, vz0, color, light, 0, -1, 0);
      vertex(pose, vc, x1, y0, z0, ux1, vz0, color, light, 0, -1, 0);
      vertex(pose, vc, x1, y0, z1, ux1, vz1, color, light, 0, -1, 0);
      vertex(pose, vc, x0, y0, z1, ux0, vz1, color, light, 0, -1, 0);
    }
    if (drawUp) {
      vertex(pose, vc, x0, y1, z1, ux0, vz1, color, light, 0, 1, 0);
      vertex(pose, vc, x1, y1, z1, ux1, vz1, color, light, 0, 1, 0);
      vertex(pose, vc, x1, y1, z0, ux1, vz0, color, light, 0, 1, 0);
      vertex(pose, vc, x0, y1, z0, ux0, vz0, color, light, 0, 1, 0);
    }
    vertex(pose, vc, x1, y0, z0, ux1, vyBottom, color, light, 0, 0, -1);
    vertex(pose, vc, x1, y1, z0, ux1, vyTop, color, light, 0, 0, -1);
    vertex(pose, vc, x0, y1, z0, ux0, vyTop, color, light, 0, 0, -1);
    vertex(pose, vc, x0, y0, z0, ux0, vyBottom, color, light, 0, 0, -1);
    vertex(pose, vc, x0, y0, z1, ux0, vyBottom, color, light, 0, 0, 1);
    vertex(pose, vc, x0, y1, z1, ux0, vyTop, color, light, 0, 0, 1);
    vertex(pose, vc, x1, y1, z1, ux1, vyTop, color, light, 0, 0, 1);
    vertex(pose, vc, x1, y0, z1, ux1, vyBottom, color, light, 0, 0, 1);
    vertex(pose, vc, x0, y0, z0, uz0, vyBottom, color, light, -1, 0, 0);
    vertex(pose, vc, x0, y1, z0, uz0, vyTop, color, light, -1, 0, 0);
    vertex(pose, vc, x0, y1, z1, uz1, vyTop, color, light, -1, 0, 0);
    vertex(pose, vc, x0, y0, z1, uz1, vyBottom, color, light, -1, 0, 0);
    vertex(pose, vc, x1, y0, z1, uz1, vyBottom, color, light, 1, 0, 0);
    vertex(pose, vc, x1, y1, z1, uz1, vyTop, color, light, 1, 0, 0);
    vertex(pose, vc, x1, y1, z0, uz0, vyTop, color, light, 1, 0, 0);
    vertex(pose, vc, x1, y0, z0, uz0, vyBottom, color, light, 1, 0, 0);
  }

  private static float clamp01(float value) {
    return Math.min(1.0f, Math.max(0.0f, value));
  }
}
