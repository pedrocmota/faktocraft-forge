package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.chunk_loader.BlockEntityChunkLoader;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ChunkBorderOverlay {

  private static final ResourceLocation FORCEFIELD = new ResourceLocation("textures/misc/forcefield.png");
  private static final float TEX_SCALE = 0.5F;

  private static boolean active = false;
  private static ResourceKey<Level> dimension;
  private static List<double[]> walls = List.of();

  private ChunkBorderOverlay() {
  }

  public static void show(BlockPos loaderPos, int chunkCount, ResourceKey<Level> dim) {
    ChunkPos base = new ChunkPos(loaderPos);
    Set<Long> chunks = new HashSet<>();
    int limit = Math.min(Math.max(chunkCount, 1), BlockEntityChunkLoader.CHUNK_OFFSETS.length);
    for (int i = 0; i < limit; i++) {
      chunks.add(ChunkPos.asLong(base.x + BlockEntityChunkLoader.CHUNK_OFFSETS[i][0],
          base.z + BlockEntityChunkLoader.CHUNK_OFFSETS[i][1]));
    }

    List<double[]> segments = new ArrayList<>();
    for (long packed : chunks) {
      int cx = ChunkPos.getX(packed);
      int cz = ChunkPos.getZ(packed);
      double x0 = cx * 16.0;
      double z0 = cz * 16.0;
      if (!chunks.contains(ChunkPos.asLong(cx, cz - 1))) {
        segments.add(new double[] {x0, z0, x0 + 16.0, z0});
      }
      if (!chunks.contains(ChunkPos.asLong(cx, cz + 1))) {
        segments.add(new double[] {x0, z0 + 16.0, x0 + 16.0, z0 + 16.0});
      }
      if (!chunks.contains(ChunkPos.asLong(cx - 1, cz))) {
        segments.add(new double[] {x0, z0, x0, z0 + 16.0});
      }
      if (!chunks.contains(ChunkPos.asLong(cx + 1, cz))) {
        segments.add(new double[] {x0 + 16.0, z0, x0 + 16.0, z0 + 16.0});
      }
    }

    walls = segments;
    dimension = dim;
    active = true;
  }

  public static void hide() {
    active = false;
    walls = List.of();
  }

  public static boolean isActive() {
    return active;
  }

  public static void onScreenOpening(ScreenEvent.Opening event) {
    if (active && event.getNewScreen() instanceof PauseScreen) {
      hide();
      event.setCanceled(true);
    }
  }

  public static void render(RenderLevelStageEvent event) {
    if (!active || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
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

    Vec3 camera = event.getCamera().getPosition();
    double minY = minecraft.level.getMinBuildHeight();
    double maxY = minecraft.level.getMaxBuildHeight();
    float anim = (Util.getMillis() % 3000L) / 3000.0F * 2.0F;
    float v0 = (float) (minY * TEX_SCALE);
    float v1 = (float) (maxY * TEX_SCALE);

    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();
    poseStack.translate(-camera.x, -camera.y, -camera.z);
    Matrix4f matrix = poseStack.last().pose();

    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    RenderSystem.disableCull();
    RenderSystem.depthMask(false);
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, FORCEFIELD);
    RenderSystem.setShaderColor(0.25F, 0.85F, 0.82F, 0.55F);

    Tesselator tesselator = Tesselator.getInstance();
    BufferBuilder buffer = tesselator.getBuilder();
    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    for (double[] wall : walls) {
      float length = (float) Math.abs(wall[2] - wall[0] + wall[3] - wall[1]);
      float u0 = anim + (float) ((wall[0] + wall[1]) * TEX_SCALE);
      float u1 = u0 + length * TEX_SCALE;
      buffer.vertex(matrix, (float) wall[0], (float) minY, (float) wall[1]).uv(u0, v0).endVertex();
      buffer.vertex(matrix, (float) wall[2], (float) minY, (float) wall[3]).uv(u1, v0).endVertex();
      buffer.vertex(matrix, (float) wall[2], (float) maxY, (float) wall[3]).uv(u1, v1).endVertex();
      buffer.vertex(matrix, (float) wall[0], (float) maxY, (float) wall[1]).uv(u0, v1).endVertex();
    }
    tesselator.end();

    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.depthMask(true);
    RenderSystem.enableCull();
    RenderSystem.defaultBlendFunc();
    RenderSystem.disableBlend();
    poseStack.popPose();
  }

  public static void renderHud(GuiGraphics graphics) {
    if (!active) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen != null) {
      return;
    }
    Component text = Component.translatable("gui.faktocraft.chunk_loader.border_exit");
    int width = minecraft.font.width(text);
    int x = (graphics.guiWidth() - width) / 2;
    int y = 16;
    graphics.fill(x - 4, y - 4, x + width + 4, y + 12, 0x90000000);
    graphics.drawString(minecraft.font, text, x, y, 0xFFFFFF, true);
  }
}
