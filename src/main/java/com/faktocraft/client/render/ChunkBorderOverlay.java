package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.chunk_loader.BlockEntityChunkLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ChunkBorderOverlay {
  private static final Identifier FORCEFIELD = Identifier.parse("textures/misc/forcefield.png");
  private static final float TEX_SCALE = 0.5F;
  private static final int COLOR_R = 64;
  private static final int COLOR_G = 217;
  private static final int COLOR_B = 209;
  private static final int COLOR_A = 140;
  private static final double WALL_OFFSET = 1.0 / 128.0;
  private static final int HUD_BOTTOM_MARGIN = 90;

  private static boolean active = false;
  private static ResourceKey<Level> dimension;
  private static BlockPos loaderPos;
  private static int shownChunkCount;
  private static List<double[]> walls = List.of();

  private ChunkBorderOverlay() {
  }

  public static void show(BlockPos loaderPos, int chunkCount, ResourceKey<Level> dim) {
    ChunkPos base = ChunkPos.containing(loaderPos);
    Set<Long> chunks = new HashSet<>();
    int limit = Math.min(Math.max(chunkCount, 1), BlockEntityChunkLoader.CHUNK_OFFSETS.length);
    for (int i = 0; i < limit; i++) {
      chunks.add(ChunkPos.pack(base.x() + BlockEntityChunkLoader.CHUNK_OFFSETS[i][0],
          base.z() + BlockEntityChunkLoader.CHUNK_OFFSETS[i][1]));
    }

    List<double[]> segments = new ArrayList<>();
    for (long packed : chunks) {
      int cx = ChunkPos.getX(packed);
      int cz = ChunkPos.getZ(packed);
      double x0 = cx * 16.0;
      double z0 = cz * 16.0;
      if (!chunks.contains(ChunkPos.pack(cx, cz - 1))) {
        segments.add(new double[] { x0, z0 - WALL_OFFSET, x0 + 16.0, z0 - WALL_OFFSET });
      }
      if (!chunks.contains(ChunkPos.pack(cx, cz + 1))) {
        segments.add(new double[] { x0, z0 + 16.0 + WALL_OFFSET, x0 + 16.0, z0 + 16.0 + WALL_OFFSET });
      }
      if (!chunks.contains(ChunkPos.pack(cx - 1, cz))) {
        segments.add(new double[] { x0 - WALL_OFFSET, z0, x0 - WALL_OFFSET, z0 + 16.0 });
      }
      if (!chunks.contains(ChunkPos.pack(cx + 1, cz))) {
        segments.add(new double[] { x0 + 16.0 + WALL_OFFSET, z0, x0 + 16.0 + WALL_OFFSET, z0 + 16.0 });
      }
    }

    walls = segments;
    dimension = dim;
    ChunkBorderOverlay.loaderPos = loaderPos.immutable();
    shownChunkCount = chunkCount;
    active = true;
  }

  public static boolean isShowing(BlockPos pos) {
    return active && pos.equals(loaderPos);
  }

  public static int shownChunkCount() {
    return shownChunkCount;
  }

  public static void hide() {
    active = false;
    walls = List.of();
    loaderPos = null;
  }

  public static void reset() {
    hide();
    dimension = null;
  }

  public static boolean isActive() {
    return active;
  }

  public static void onScreenOpening(ScreenEvent.Opening event) {
    if (active && event.getNewScreen() instanceof PauseScreen && Minecraft.getInstance().isWindowActive()) {
      hide();
      event.setCanceled(true);
    }
  }

  public static void submit(SubmitCustomGeometryEvent event) {
    if (!active) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null) {
      hide();
      return;
    }
    if (!minecraft.level.dimension().equals(dimension) || walls.isEmpty()) {
      return;
    }

    Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
    double minY = minecraft.level.getMinY();
    double maxY = minecraft.level.getMaxY() + 1;
    float anim = (Util.getMillis() % 3000L) / 3000.0F * 2.0F;
    float v0 = (float) (minY * TEX_SCALE);
    float v1 = (float) (maxY * TEX_SCALE);
    List<double[]> segments = walls;

    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();
    poseStack.translate(-camera.x, -camera.y, -camera.z);
    event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.beaconBeam(FORCEFIELD, true),
        (pose, buffer) -> {
          for (double[] wall : segments) {
            float length = (float) Math.abs(wall[2] - wall[0] + wall[3] - wall[1]);
            float u0 = anim + (float) ((wall[0] + wall[1]) * TEX_SCALE);
            float u1 = u0 + length * TEX_SCALE;
            float x0 = (float) wall[0];
            float z0 = (float) wall[1];
            float x1 = (float) wall[2];
            float z1 = (float) wall[3];
            float y0 = (float) minY;
            float y1 = (float) maxY;
            vertex(buffer, pose, x0, y0, z0, u0, v0);
            vertex(buffer, pose, x1, y0, z1, u1, v0);
            vertex(buffer, pose, x1, y1, z1, u1, v1);
            vertex(buffer, pose, x0, y1, z0, u0, v1);
            vertex(buffer, pose, x0, y1, z0, u0, v1);
            vertex(buffer, pose, x1, y1, z1, u1, v1);
            vertex(buffer, pose, x1, y0, z1, u1, v0);
            vertex(buffer, pose, x0, y0, z0, u0, v0);
          }
        });
    poseStack.popPose();
  }

  private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u,
      float v) {
    buffer.addVertex(pose, x, y, z)
        .setColor(COLOR_R, COLOR_G, COLOR_B, COLOR_A)
        .setUv(u, v)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(LightCoordsUtil.FULL_BRIGHT)
        .setNormal(pose, 0.0F, 1.0F, 0.0F);
  }

  public static void renderHud(GuiGraphicsExtractor graphics) {
    if (!active) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.gui.screen() != null) {
      return;
    }
    Component text = Component.translatable("gui.faktocraft.chunk_loader.border_exit");
    int width = minecraft.font.width(text);
    int x = (graphics.guiWidth() - width) / 2;
    int y = graphics.guiHeight() - HUD_BOTTOM_MARGIN;
    graphics.fill(x - 4, y - 4, x + width + 4, y + 12, 0x90000000);
    graphics.text(minecraft.font, text, x, y, 0xFFFFFFFF, true);
  }
}
