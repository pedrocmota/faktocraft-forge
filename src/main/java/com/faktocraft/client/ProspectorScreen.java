package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.impl.tools.Prospector;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketProspectorScan;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.ArrayList;
import java.util.List;

public class ProspectorScreen extends Screen {

  private static final int GRID = 7;
  private static final int CELL = 22;
  private static final int BLOCKS = GRID * 16;
  private static final int MAP_SIZE = GRID * CELL;

  private static final int PANEL_W = MAP_SIZE + 24;
  private static final int PANEL_H = MAP_SIZE + 86;

  private int panelLeft;
  private int panelTop;
  private Button scanButton;
  private DynamicTexture mapTexture;
  private ResourceLocation mapLocation;
  private int mapOriginChunkX;
  private int mapOriginChunkZ;

  public ProspectorScreen() {
    super(Component.translatable("item." + Faktocraft.MODID + ".prospector"));
  }

  public static void drawDeviceFrame(GuiGraphics graphics, int left, int top, int w, int h) {
    graphics.fill(left - 1, top - 1, left + w + 1, top, 0xFF000000);
    graphics.fill(left - 1, top + h, left + w + 1, top + h + 1, 0xFF000000);
    graphics.fill(left - 1, top, left, top + h, 0xFF000000);
    graphics.fill(left + w, top, left + w + 1, top + h, 0xFF000000);
    graphics.fill(left, top, left + w, top + h, 0xFF41454C);
    graphics.fill(left, top, left + w, top + 2, 0xFF6E747D);
    graphics.fill(left, top, left + 2, top + h, 0xFF5C626A);
    graphics.fill(left, top + h - 2, left + w, top + h, 0xFF23262B);
    graphics.fill(left + w - 2, top, left + w, top + h, 0xFF2B2F35);
    for (int[] corner : new int[][] { { 3, 3 }, { w - 5, 3 }, { 3, h - 5 }, { w - 5, h - 5 } }) {
      int sx = left + corner[0];
      int sy = top + corner[1];
      graphics.fill(sx, sy, sx + 2, sy + 2, 0xFF9AA0A8);
      graphics.fill(sx + 1, sy + 1, sx + 2, sy + 2, 0xFF585E66);
    }
    graphics.fill(left + 6, top + 6, left + w - 6, top + h - 6, 0xFF06080A);
    graphics.fill(left + 7, top + 7, left + w - 7, top + h - 7, 0xF0101014);
    graphics.fill(left + 7, top + 7, left + w - 7, top + 8, 0xFF000000);
    graphics.fill(left + 7, top + h - 8, left + w - 7, top + h - 7, 0xFF32363C);
  }

  public static ItemStack heldProspector(@org.jetbrains.annotations.Nullable Player player) {
    if (player == null) {
      return ItemStack.EMPTY;
    }
    if (player.getMainHandItem().getItem() instanceof Prospector) {
      return player.getMainHandItem();
    }
    if (player.getOffhandItem().getItem() instanceof Prospector) {
      return player.getOffhandItem();
    }
    return ItemStack.EMPTY;
  }

  @Override
  protected void init() {
    panelLeft = (width - PANEL_W) / 2;
    panelTop = (height - PANEL_H) / 2;
    scanButton = addRenderableWidget(Button.builder(
        Component.translatable("gui." + Faktocraft.MODID + ".prospector.scan"),
        button -> ModNetworking.sendToServer(PacketProspectorScan.INSTANCE))
        .bounds(panelLeft + 12, panelTop + PANEL_H - 32, PANEL_W - 24, 20)
        .build());
    buildSurfaceMap();
  }

  private void buildSurfaceMap() {
    if (minecraft == null || minecraft.level == null || minecraft.player == null) {
      return;
    }
    closeMapTexture();
    mapOriginChunkX = minecraft.player.chunkPosition().x - GRID / 2;
    mapOriginChunkZ = minecraft.player.chunkPosition().z - GRID / 2;
    int baseX = mapOriginChunkX << 4;
    int baseZ = mapOriginChunkZ << 4;

    NativeImage image = new NativeImage(BLOCKS, BLOCKS, false);
    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    for (int z = 0; z < BLOCKS; z++) {
      for (int x = 0; x < BLOCKS; x++) {
        int worldX = baseX + x;
        int worldZ = baseZ + z;
        int h = minecraft.level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
        int rgb = 0x000000;
        if (h > minecraft.level.getMinBuildHeight()) {
          cursor.set(worldX, h - 1, worldZ);
          rgb = minecraft.level.getBlockState(cursor).getMapColor(minecraft.level, cursor).col;
          int hNorth = minecraft.level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ - 1);
          float shade = h > hNorth ? 1.14F : h < hNorth ? 0.82F : 1.0F;
          int r = Mth.clamp((int) (((rgb >> 16) & 0xFF) * shade), 0, 255);
          int g = Mth.clamp((int) (((rgb >> 8) & 0xFF) * shade), 0, 255);
          int b = Mth.clamp((int) ((rgb & 0xFF) * shade), 0, 255);
          rgb = (r << 16) | (g << 8) | b;
        }
        image.setPixelRGBA(x, z, 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF));
      }
    }
    mapTexture = new DynamicTexture(image);
    mapLocation = minecraft.getTextureManager().register("prospector_map", mapTexture);
  }

  private void closeMapTexture() {
    if (mapTexture != null && minecraft != null) {
      minecraft.getTextureManager().release(mapLocation);
      mapTexture = null;
      mapLocation = null;
    }
  }

  @Override
  public void removed() {
    closeMapTexture();
    super.removed();
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);

    drawDeviceFrame(graphics, panelLeft, panelTop, PANEL_W, PANEL_H);

    graphics.drawCenteredString(font, title, panelLeft + PANEL_W / 2, panelTop + 11, 0xFFE8C43A);

    ItemStack stack = heldProspector(minecraft.player);
    Player player = minecraft.player;
    if (stack.isEmpty() || player == null) {
      onClose();
      return;
    }

    int gridLeft = panelLeft + 12;
    int gridTop = panelTop + 22;

    if (mapLocation != null) {
      graphics.pose().pushPose();
      graphics.pose().translate(gridLeft, gridTop, 0);
      float scale = MAP_SIZE / (float) BLOCKS;
      graphics.pose().scale(scale, scale, 1.0F);
      graphics.blit(mapLocation, 0, 0, 0, 0, BLOCKS, BLOCKS, BLOCKS, BLOCKS);
      graphics.pose().popPose();
    }

    CompoundTag job = Prospector.getJob(stack);
    int playerChunkX = player.chunkPosition().x;
    int playerChunkZ = player.chunkPosition().z;
    List<Component> hoverLines = null;

    for (int gz = 0; gz < GRID; gz++) {
      for (int gx = 0; gx < GRID; gx++) {
        int chunkX = mapOriginChunkX + gx;
        int chunkZ = mapOriginChunkZ + gz;
        int cx = gridLeft + gx * CELL;
        int cy = gridTop + gz * CELL;
        CompoundTag scan = Prospector.getScan(stack, Prospector.chunkKey(player.level(), chunkX, chunkZ));

        if (scan == null) {
          graphics.fill(cx, cy, cx + CELL, cy + CELL, 0x66101014);
        } else {
          CompoundTag entries = scan.getCompound("entries");
          int total = 0;
          for (String key : entries.getAllKeys()) {
            total += entries.getInt(key);
          }
          if (total == 0) {
            graphics.fill(cx, cy, cx + CELL, cy + CELL, 0x50204060);
          } else {
            float heat = Mth.clamp(total / 400.0F, 0.0F, 1.0F);
            int r = (int) (60 + heat * 195);
            int g = (int) (150 + heat * 60);
            graphics.fill(cx, cy, cx + CELL, cy + CELL, 0x58000000 | (r << 16) | (g << 8) | 45);
          }
        }

        graphics.fill(cx, cy, cx + CELL, cy + 1, 0x50000000);
        graphics.fill(cx, cy, cx + 1, cy + CELL, 0x50000000);

        if (job != null && job.getInt("cx") == chunkX && job.getInt("cz") == chunkZ) {
          float pulse = 0.55F + 0.45F * Mth.sin((player.tickCount + partialTick) * 0.35F);
          int alpha = (int) (pulse * 255) << 24;
          int gold = alpha | 0xE8C43A;
          graphics.fill(cx, cy, cx + CELL, cy + 1, gold);
          graphics.fill(cx, cy + CELL - 1, cx + CELL, cy + CELL, gold);
          graphics.fill(cx, cy, cx + 1, cy + CELL, gold);
          graphics.fill(cx + CELL - 1, cy, cx + CELL, cy + CELL, gold);
        } else if (chunkX == playerChunkX && chunkZ == playerChunkZ) {
          graphics.fill(cx, cy, cx + CELL, cy + 1, 0xFFFFFFFF);
          graphics.fill(cx, cy + CELL - 1, cx + CELL, cy + CELL, 0xFFFFFFFF);
          graphics.fill(cx, cy, cx + 1, cy + CELL, 0xFFFFFFFF);
          graphics.fill(cx + CELL - 1, cy, cx + CELL, cy + CELL, 0xFFFFFFFF);
        }

        if (mouseX >= cx && mouseX < cx + CELL && mouseY >= cy && mouseY < cy + CELL) {
          graphics.fill(cx, cy, cx + CELL, cy + CELL, 0x28FFFFFF);
          hoverLines = buildHoverSummary(chunkX, chunkZ, scan);
        }
      }
    }
    graphics.fill(gridLeft, gridTop + MAP_SIZE - 1, gridLeft + MAP_SIZE, gridTop + MAP_SIZE, 0x50000000);
    graphics.fill(gridLeft + MAP_SIZE - 1, gridTop, gridLeft + MAP_SIZE, gridTop + MAP_SIZE, 0x50000000);

    int energy = ModComponents.getEnergy(stack, 0);
    boolean canAfford = energy >= Prospector.SCAN_COST;
    graphics.drawCenteredString(font,
        Component.literal(TextComponentUtil.getFormattedEnergyUnit(energy) + " / "
            + TextComponentUtil.getFormattedEnergyUnit(400000) + " IE")
            .withStyle(canAfford ? ChatFormatting.AQUA : ChatFormatting.RED),
        panelLeft + PANEL_W / 2, panelTop + PANEL_H - 46, 0xFFFFFF);
    if (!canAfford && job == null) {
      graphics.drawCenteredString(font,
          Component.translatable("gui." + Faktocraft.MODID + ".prospector.no_energy")
              .withStyle(ChatFormatting.RED),
          panelLeft + PANEL_W / 2, panelTop + PANEL_H - 58, 0xFFFFFF);
    }

    scanButton.visible = job == null;
    scanButton.active = canAfford;
    if (job != null) {
      int barLeft = panelLeft + 12;
      int barTop = panelTop + PANEL_H - 32;
      int barW = PANEL_W - 24;
      float progress = 1.0F - job.getInt("remaining") / (float) Math.max(1, job.getInt("total"));
      graphics.fill(barLeft, barTop, barLeft + barW, barTop + 20, 0xFF26262C);
      graphics.fill(barLeft + 1, barTop + 1, barLeft + 1 + (int) ((barW - 2) * progress), barTop + 19, 0xFF7F9E32);
      graphics.drawCenteredString(font,
          Component.translatable("gui." + Faktocraft.MODID + ".prospector.scanning",
              (int) (progress * 100)),
          panelLeft + PANEL_W / 2, barTop + 6, 0xFFFFFF);
    }

    super.render(graphics, mouseX, mouseY, partialTick);

    if (hoverLines != null) {
      graphics.renderComponentTooltip(font, hoverLines, mouseX, mouseY);
    }
  }

  private List<Component> buildHoverSummary(int chunkX, int chunkZ,
      @org.jetbrains.annotations.Nullable CompoundTag scan) {
    List<Component> lines = new ArrayList<>();
    lines.add(Component.literal("Chunk " + chunkX + ", " + chunkZ).withStyle(ChatFormatting.WHITE));
    if (scan == null) {
      lines.add(Component.translatable("gui." + Faktocraft.MODID + ".prospector.not_scanned")
          .withStyle(ChatFormatting.DARK_GRAY));
      return lines;
    }
    CompoundTag entries = scan.getCompound("entries");
    int total = 0;
    for (String key : entries.getAllKeys()) {
      total += entries.getInt(key);
    }
    if (entries.isEmpty()) {
      lines.add(Component.translatable("gui." + Faktocraft.MODID + ".prospector.empty")
          .withStyle(ChatFormatting.GRAY));
    } else {
      lines.add(Component.translatable("gui." + Faktocraft.MODID + ".prospector.summary",
          entries.getAllKeys().size(), total).withStyle(ChatFormatting.YELLOW));
    }
    lines.add(Component.translatable("gui." + Faktocraft.MODID + ".prospector.details_hint")
        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    return lines;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    if (button == 0 && minecraft != null && minecraft.player != null) {
      int gridLeft = panelLeft + 12;
      int gridTop = panelTop + 22;
      if (mouseX >= gridLeft && mouseX < gridLeft + MAP_SIZE && mouseY >= gridTop && mouseY < gridTop + MAP_SIZE) {
        int gx = (int) ((mouseX - gridLeft) / CELL);
        int gz = (int) ((mouseY - gridTop) / CELL);
        int chunkX = mapOriginChunkX + gx;
        int chunkZ = mapOriginChunkZ + gz;
        ItemStack stack = heldProspector(minecraft.player);
        if (!stack.isEmpty() && Prospector.getScan(stack,
            Prospector.chunkKey(minecraft.player.level(), chunkX, chunkZ)) != null) {
          minecraft.setScreen(new ProspectorDetailScreen(chunkX, chunkZ));
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
