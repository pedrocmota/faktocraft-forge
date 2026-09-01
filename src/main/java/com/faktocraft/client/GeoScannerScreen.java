package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketGeoScannerControl;
import com.faktocraft.common.network.packet.PacketGeoScannerPoll;
import com.faktocraft.common.network.packet.PacketGeoScannerState;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public class GeoScannerScreen extends Screen {

  private static final int RADIUS = BlockEntityGeoScanner.RADIUS;
  private static final int SPAN = 2 * RADIUS + 1;
  private static final int PAN_STEP = 3;

  private static final int SIDE = 18;
  private static final int MAP_TOP = 34;
  private static final int BOTTOM_PAD = 18;

  private static final int FOOTER_H = 72;
  private static final int MIN_CELL = 10;
  private static final int MAX_CELL = 40;

  private final BlockPos pos;
  private final int centerChunkX;
  private final int centerChunkZ;

  private int revision;
  private boolean running;
  private int energy;
  private int capacity;
  private int scanned;
  private boolean jobActive;
  private int jobCx;
  private int jobCz;
  private int jobRemaining;
  private boolean manualPending;
  private int manualCx;
  private int manualCz;
  private CompoundTag scans = new CompoundTag();

  private int cell = 26;
  private int gridW;
  private int gridH;

  private int viewX;
  private int viewZ;
  private boolean viewInitialized;

  private int mapW;
  private int mapH;
  private int drawnW;
  private int drawnH;
  private int panelW;
  private int panelH;
  private int panelLeft;
  private int panelTop;

  private Button toggleButton;
  private DynamicTexture mapTexture;
  private ResourceLocation mapLocation;
  private int pollCooldown;

  private boolean dragging;
  private double dragAccumX;
  private double dragAccumZ;
  private double dragTotal;
  private long lastClickMs;
  private int lastClickCx;
  private int lastClickCz;

  private record RareFind(boolean iridium, int chunkX, int chunkZ) {
  }

  private final List<RareFind> rareFinds = new ArrayList<>();
  private int rareCycle;
  private int chipX;
  private int chipY;
  private int chipW;
  private int chipH;

  public GeoScannerScreen(PacketGeoScannerState state) {
    super(Component.translatable("block." + Faktocraft.MODID + ".geological_scanner"));
    this.pos = state.blockPos();
    ChunkPos center = new ChunkPos(pos);
    this.centerChunkX = center.x;
    this.centerChunkZ = center.z;
    applyState(state);
  }

  public boolean matches(BlockPos other) {
    return pos.equals(other);
  }

  public void applyState(PacketGeoScannerState state) {
    revision = state.revision();
    running = state.running();
    energy = state.energy();
    capacity = state.capacity();
    scanned = state.scanned();
    jobActive = state.jobActive();
    jobCx = state.jobCx();
    jobCz = state.jobCz();
    jobRemaining = state.jobRemaining();
    manualPending = state.manualPending();
    manualCx = state.manualCx();
    manualCz = state.manualCz();
    if (state.scans() != null) {
      scans = state.scans();
      rebuildRareFinds();
    }
    if (toggleButton != null) {
      toggleButton.setMessage(toggleLabel());
    }
  }

  private static boolean isIridium(String id) {
    return id.contains("iridium");
  }

  private static boolean isGiantOil(String id) {
    return id.equals("faktocraft:oil_giant");
  }

  private void rebuildRareFinds() {
    rareFinds.clear();
    for (String key : scans.getAllKeys()) {
      CompoundTag entries = scans.getCompound(key).getCompound("entries");
      boolean iridium = false;
      boolean oil = false;
      for (String id : entries.getAllKeys()) {
        iridium |= isIridium(id);
        oil |= isGiantOil(id);
      }
      if (iridium || oil) {
        String[] parts = key.split(",");
        int cx = Integer.parseInt(parts[0]);
        int cz = Integer.parseInt(parts[1]);
        if (iridium) {
          rareFinds.add(new RareFind(true, cx, cz));
        }
        if (oil) {
          rareFinds.add(new RareFind(false, cx, cz));
        }
      }
    }
  }

  private Component toggleLabel() {
    return Component.translatable("gui." + Faktocraft.MODID
        + (running ? ".geo_scanner.pause" : ".geo_scanner.start"));
  }

  private Component infoLine(int scannedValue, int energyValue) {
    return Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.progress",
        scannedValue, BlockEntityGeoScanner.TOTAL_CHUNKS).withStyle(ChatFormatting.GRAY)
        .append(Component.literal("  ·  ").withStyle(ChatFormatting.DARK_GRAY))
        .append(Component.literal(TextComponentUtil.getFormattedEnergyUnit(energyValue) + " / "
            + TextComponentUtil.getFormattedEnergyUnit(capacity) + " IE")
            .withStyle(energyValue >= BlockEntityGeoScanner.SCAN_COST ? ChatFormatting.AQUA : ChatFormatting.RED));
  }

  private int mapLeft() {
    return panelLeft + SIDE + (mapW - drawnW) / 2;
  }

  private int mapTop() {
    return panelTop + MAP_TOP + (mapH - drawnH) / 2;
  }

  @Override
  protected void init() {
    int infoW = font.width(infoLine(BlockEntityGeoScanner.TOTAL_CHUNKS, capacity)) + 12;
    int titleW = font.width(title) + 12;
    int labelW = Math.max(
        Math.max(font.width(Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.start")),
            font.width(Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.pause"))),
        font.width(Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.center")));
    int textNeed = Math.max(Math.max(infoW, titleW), 2 * (labelW + 18) + 8);

    mapH = Math.max(7 * MIN_CELL, height - MAP_TOP - FOOTER_H - BOTTOM_PAD - 16);

    mapW = Math.max(textNeed, Math.min((int) (mapH * 1.15F), width - 2 * SIDE - 8));

    panelW = mapW + 2 * SIDE;
    panelH = MAP_TOP + mapH + FOOTER_H + BOTTOM_PAD;
    panelLeft = (width - panelW) / 2;
    panelTop = (height - panelH) / 2;

    recomputeViewport(true);

    int half = (panelW - 2 * SIDE - 8) / 2;
    int buttonsY = panelTop + panelH - BOTTOM_PAD - 20;
    toggleButton = addRenderableWidget(new DeviceButton(panelLeft + SIDE, buttonsY, half, 20,
        toggleLabel(), button -> {
          running = !running;
          button.setMessage(toggleLabel());
          ModNetworking.sendToServer(new PacketGeoScannerControl(pos, running));
        }));
    addRenderableWidget(new DeviceButton(panelLeft + SIDE + half + 8, buttonsY, half, 20,
        Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.center"),
        button -> {
          viewX = -gridW / 2;
          viewZ = -gridH / 2;
          clampView();
          buildSurfaceMap();
        }));
  }

  private static final int MIN_VISIBLE_ROWS = 6;

  private int maxCell() {
    return Math.max(MIN_CELL, Math.min(MAX_CELL, mapH / MIN_VISIBLE_ROWS));
  }

  private void recomputeViewport(boolean keepCenter) {
    int oldCenterX = viewX + gridW / 2;
    int oldCenterZ = viewZ + gridH / 2;
    cell = Mth.clamp(cell, MIN_CELL, maxCell());

    gridW = Mth.clamp((mapW + cell - 1) / cell, 1, SPAN);
    gridH = Mth.clamp((mapH + cell - 1) / cell, 1, SPAN);
    drawnW = Math.min(mapW, gridW * cell);
    drawnH = Math.min(mapH, gridH * cell);
    if (!viewInitialized) {
      viewInitialized = true;
      viewX = -gridW / 2;
      viewZ = -gridH / 2;
    } else if (keepCenter) {
      viewX = oldCenterX - gridW / 2;
      viewZ = oldCenterZ - gridH / 2;
    }
    clampView();
    buildSurfaceMap();
  }

  private void setZoom(int newCell) {
    newCell = Mth.clamp(newCell, MIN_CELL, maxCell());
    if (newCell != cell) {
      cell = newCell;
      recomputeViewport(true);
    }
  }

  @Override
  public void tick() {
    if (--pollCooldown <= 0) {
      pollCooldown = 20;
      ModNetworking.sendToServer(new PacketGeoScannerPoll(pos, revision));
    }
  }

  private void clampView() {
    viewX = Mth.clamp(viewX, -RADIUS, RADIUS - (gridW - 1));
    viewZ = Mth.clamp(viewZ, -RADIUS, RADIUS - (gridH - 1));
  }

  private void pan(int dx, int dz) {
    int oldX = viewX;
    int oldZ = viewZ;
    viewX += dx;
    viewZ += dz;
    clampView();
    if (viewX != oldX || viewZ != oldZ) {
      buildSurfaceMap();
    }
  }

  private void buildSurfaceMap() {
    if (minecraft == null || minecraft.level == null) {
      return;
    }
    closeMapTexture();
    int blocksW = gridW * 16;
    int blocksH = gridH * 16;
    int baseX = (centerChunkX + viewX) << 4;
    int baseZ = (centerChunkZ + viewZ) << 4;
    NativeImage image = new NativeImage(blocksW, blocksH, false);
    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    for (int z = 0; z < blocksH; z++) {
      for (int x = 0; x < blocksW; x++) {
        int worldX = baseX + x;
        int worldZ = baseZ + z;

        int rgb = ((x / 8) + (z / 8)) % 2 == 0 ? 0x1E2126 : 0x23262C;
        if (minecraft.level.hasChunk((worldX >> 4), (worldZ >> 4))) {
          int h = minecraft.level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
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
        }
        image.setPixelRGBA(x, z, 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF));
      }
    }
    mapTexture = new DynamicTexture(image);
    mapLocation = minecraft.getTextureManager().register("geo_scanner_map", mapTexture);
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

    ProspectorScreen.drawDeviceFrame(graphics, panelLeft, panelTop, panelW, panelH);
    graphics.drawCenteredString(font, title, panelLeft + panelW / 2, panelTop + 14, 0xFFE8C43A);

    int gridLeft = mapLeft();
    int gridTop = mapTop();

    graphics.enableScissor(gridLeft, gridTop, gridLeft + drawnW, gridTop + drawnH);
    if (mapLocation != null) {
      int blocksW = gridW * 16;
      int blocksH = gridH * 16;
      graphics.pose().pushPose();
      graphics.pose().translate(gridLeft, gridTop, 0);
      float scale = cell / 16.0F;
      graphics.pose().scale(scale, scale, 1.0F);
      graphics.blit(mapLocation, 0, 0, 0, 0, blocksW, blocksH, blocksW, blocksH);
      graphics.pose().popPose();
    }

    List<Component> hoverLines = null;
    long tick = minecraft != null && minecraft.player != null ? minecraft.player.tickCount : 0;

    for (int gz = 0; gz < gridH; gz++) {
      for (int gx = 0; gx < gridW; gx++) {
        int chunkX = centerChunkX + viewX + gx;
        int chunkZ = centerChunkZ + viewZ + gz;
        int cx = gridLeft + gx * cell;
        int cy = gridTop + gz * cell;
        if (outsideScanArea(chunkX, chunkZ)) {
          graphics.fill(cx, cy, cx + cell, cy + cell, 0xE0080A0C);
          continue;
        }
        CompoundTag scan = scans.contains(BlockEntityGeoScanner.scanKey(chunkX, chunkZ))
            ? scans.getCompound(BlockEntityGeoScanner.scanKey(chunkX, chunkZ))
            : null;

        if (scan == null) {
          graphics.fill(cx, cy, cx + cell, cy + cell, 0x66101014);
        } else {
          CompoundTag entries = scan.getCompound("entries");
          int total = 0;
          for (String key : entries.getAllKeys()) {
            total += entries.getInt(key);
          }
          if (total == 0) {
            graphics.fill(cx, cy, cx + cell, cy + cell, 0x50204060);
          } else {
            float heat = Mth.clamp(total / 400.0F, 0.0F, 1.0F);
            int r = (int) (60 + heat * 195);
            int g = (int) (150 + heat * 60);
            graphics.fill(cx, cy, cx + cell, cy + cell, 0x58000000 | (r << 16) | (g << 8) | 45);
          }
        }

        graphics.fill(cx, cy, cx + cell, cy + 1, 0x50000000);
        graphics.fill(cx, cy, cx + 1, cy + cell, 0x50000000);

        if (scan != null) {
          CompoundTag entries = scan.getCompound("entries");
          boolean iridium = false;
          boolean oil = false;
          for (String id : entries.getAllKeys()) {
            iridium |= isIridium(id);
            oil |= isGiantOil(id);
          }
          if (iridium || oil) {
            int r = Math.max(2, cell / 6);
            float pulse = 0.7F + 0.3F * Mth.sin((tick + partialTick) * 0.3F);
            int gold = ((int) (pulse * 255) << 24) | 0xE8C43A;
            if (iridium && oil) {
              drawDiamond(graphics, cx + cell / 3, cy + cell / 3, r, 0xFF14161A, gold);
              drawDiamond(graphics, cx + 2 * cell / 3, cy + 2 * cell / 3, r, gold, 0xFF14161A);
            } else if (iridium) {
              drawDiamond(graphics, cx + cell / 2, cy + cell / 2, r, 0xFF14161A, gold);
            } else {
              drawDiamond(graphics, cx + cell / 2, cy + cell / 2, r, gold, 0xFF14161A);
            }
          }
        }

        if (jobActive && jobCx == chunkX && jobCz == chunkZ) {
          float pulse = 0.55F + 0.45F * Mth.sin((tick + partialTick) * 0.35F);
          int alpha = (int) (pulse * 255) << 24;
          drawCellBorder(graphics, cx, cy, alpha | 0xE8C43A);
        } else if (manualPending && manualCx == chunkX && manualCz == chunkZ) {
          float pulse = 0.55F + 0.45F * Mth.sin((tick + partialTick) * 0.35F);
          int alpha = (int) (pulse * 255) << 24;
          drawCellBorder(graphics, cx, cy, alpha | 0x4FC3F7);
        } else if (chunkX == centerChunkX && chunkZ == centerChunkZ) {
          drawCellBorder(graphics, cx, cy, 0xFFFFFFFF);
        }

        boolean known = scan != null
            || (minecraft != null && minecraft.level != null && minecraft.level.hasChunk(chunkX, chunkZ));
        if (known && mouseX >= cx && mouseX < cx + cell && mouseY >= cy && mouseY < cy + cell) {
          graphics.fill(cx, cy, cx + cell, cy + cell, 0x28FFFFFF);
          hoverLines = buildHoverSummary(chunkX, chunkZ, scan);
        }
      }
    }
    graphics.fill(gridLeft, gridTop + drawnH - 1, gridLeft + drawnW, gridTop + drawnH, 0x50000000);
    graphics.fill(gridLeft + drawnW - 1, gridTop, gridLeft + drawnW, gridTop + drawnH, 0x50000000);
    graphics.disableScissor();

    if (!rareFinds.isEmpty()) {
      Component chip = Component.literal("★ " + rareFinds.size());
      chipW = font.width(chip) + 10;
      chipH = 14;
      chipX = gridLeft + 4;
      chipY = gridTop + 4;
      boolean overChip = mouseX >= chipX && mouseX < chipX + chipW && mouseY >= chipY && mouseY < chipY + chipH;
      graphics.fill(chipX, chipY, chipX + chipW, chipY + chipH, 0xD0101014);
      int chipBorder = overChip ? 0xFFE8C43A : 0xFF6E747D;
      graphics.fill(chipX, chipY, chipX + chipW, chipY + 1, chipBorder);
      graphics.fill(chipX, chipY + chipH - 1, chipX + chipW, chipY + chipH, chipBorder);
      graphics.fill(chipX, chipY, chipX + 1, chipY + chipH, chipBorder);
      graphics.fill(chipX + chipW - 1, chipY, chipX + chipW, chipY + chipH, chipBorder);
      graphics.drawString(font, chip, chipX + 5, chipY + 3, 0xFFE8C43A);
      if (overChip) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.rare_header")
            .withStyle(ChatFormatting.GOLD));
        for (RareFind find : rareFinds) {
          Component name = find.iridium()
              ? Component.translatable("item." + Faktocraft.MODID + ".iridium")
              : Component.translatable("gui." + Faktocraft.MODID + ".prospector.oil_giant");
          lines.add(name.copy().withStyle(find.iridium() ? ChatFormatting.GOLD : ChatFormatting.YELLOW)
              .append(Component.literal(" · " + find.chunkX() + ", " + find.chunkZ())
                  .withStyle(ChatFormatting.GRAY)));
        }
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.rare_hint")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        hoverLines = lines;
      }
    } else {
      chipW = 0;
      chipH = 0;
    }

    Component status;
    ChatFormatting statusColor;
    if (jobActive) {
      status = Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.status_scanning", jobCx, jobCz);
      statusColor = ChatFormatting.GREEN;
    } else if (running) {
      status = Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.status_no_energy");
      statusColor = ChatFormatting.RED;
    } else if (scanned >= BlockEntityGeoScanner.TOTAL_CHUNKS) {
      status = Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.status_done");
      statusColor = ChatFormatting.AQUA;
    } else {
      status = Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.status_paused");
      statusColor = ChatFormatting.YELLOW;
    }
    int footerTop = panelTop + MAP_TOP + mapH;
    graphics.drawCenteredString(font, status.copy().withStyle(statusColor),
        panelLeft + panelW / 2, footerTop + 8, 0xFFFFFF);

    graphics.drawCenteredString(font, infoLine(scanned, energy),
        panelLeft + panelW / 2, footerTop + 22, 0xFFFFFF);

    int barLeft = panelLeft + SIDE;
    int barTop = footerTop + 34;
    int barW = panelW - 2 * SIDE;
    graphics.fill(barLeft, barTop, barLeft + barW, barTop + 8, 0xFF26262C);
    if (jobActive) {
      float progress = 1.0F - jobRemaining / (float) BlockEntityGeoScanner.SCAN_DURATION_TICKS;
      graphics.fill(barLeft + 1, barTop + 1, barLeft + 1 + (int) ((barW - 2) * progress), barTop + 7, 0xFF7F9E32);
    }

    super.render(graphics, mouseX, mouseY, partialTick);

    if (hoverLines != null && !dragging) {
      graphics.renderComponentTooltip(font, hoverLines, mouseX, mouseY);
    }
  }

  private void drawDiamond(GuiGraphics graphics, int centerX, int centerY, int r, int fill, int outline) {
    for (int dy = -r - 1; dy <= r + 1; dy++) {
      int w = r + 1 - Math.abs(dy);
      if (w >= 0) {
        graphics.fill(centerX - w, centerY + dy, centerX + w + 1, centerY + dy + 1, outline);
      }
    }
    for (int dy = -r; dy <= r; dy++) {
      int w = r - Math.abs(dy);
      graphics.fill(centerX - w, centerY + dy, centerX + w + 1, centerY + dy + 1, fill);
    }
  }

  private void drawCellBorder(GuiGraphics graphics, int cx, int cy, int color) {
    graphics.fill(cx, cy, cx + cell, cy + 1, color);
    graphics.fill(cx, cy + cell - 1, cx + cell, cy + cell, color);
    graphics.fill(cx, cy, cx + 1, cy + cell, color);
    graphics.fill(cx + cell - 1, cy, cx + cell, cy + cell, color);
  }

  private List<Component> buildHoverSummary(int chunkX, int chunkZ,
      @org.jetbrains.annotations.Nullable CompoundTag scan) {
    List<Component> lines = new ArrayList<>();
    lines.add(Component.literal("Chunk " + chunkX + ", " + chunkZ).withStyle(ChatFormatting.WHITE));
    if (scan == null) {
      lines.add(Component.translatable("gui." + Faktocraft.MODID + ".prospector.not_scanned")
          .withStyle(ChatFormatting.DARK_GRAY));
      lines.add(Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.manual_hint")
          .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    } else {
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
    }
    lines.add(Component.translatable("gui." + Faktocraft.MODID + ".geo_scanner.pan_hint")
        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    return lines;
  }

  private boolean outsideScanArea(int chunkX, int chunkZ) {
    return Math.max(Math.abs(chunkX - centerChunkX), Math.abs(chunkZ - centerChunkZ)) > RADIUS;
  }

  private boolean overMap(double mouseX, double mouseY) {
    int gridLeft = mapLeft();
    int gridTop = mapTop();
    return mouseX >= gridLeft && mouseX < gridLeft + drawnW && mouseY >= gridTop && mouseY < gridTop + drawnH;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    if (button == 0 && chipW > 0 && mouseX >= chipX && mouseX < chipX + chipW
        && mouseY >= chipY && mouseY < chipY + chipH) {
      RareFind find = rareFinds.get(rareCycle % rareFinds.size());
      rareCycle++;
      viewX = (find.chunkX() - centerChunkX) - gridW / 2;
      viewZ = (find.chunkZ() - centerChunkZ) - gridH / 2;
      clampView();
      buildSurfaceMap();
      return true;
    }
    if (button == 0 && overMap(mouseX, mouseY)) {
      dragging = true;
      dragAccumX = 0;
      dragAccumZ = 0;
      dragTotal = 0;
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (dragging && button == 0) {
      dragTotal += Math.abs(dragX) + Math.abs(dragY);
      dragAccumX -= dragX;
      dragAccumZ -= dragY;
      int stepX = (int) (dragAccumX / cell);
      int stepZ = (int) (dragAccumZ / cell);
      if (stepX != 0 || stepZ != 0) {
        dragAccumX -= stepX * cell;
        dragAccumZ -= stepZ * cell;
        pan(stepX, stepZ);
      }
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (dragging && button == 0) {
      dragging = false;
      if (dragTotal < 4 && overMap(mouseX, mouseY)) {
        int gx = (int) ((mouseX - mapLeft()) / cell);
        int gz = (int) ((mouseY - mapTop()) / cell);
        int chunkX = centerChunkX + viewX + gx;
        int chunkZ = centerChunkZ + viewZ + gz;
        if (outsideScanArea(chunkX, chunkZ)) {
          return true;
        }
        String key = BlockEntityGeoScanner.scanKey(chunkX, chunkZ);
        if (scans.contains(key) && minecraft != null) {
          minecraft.setScreen(new GeoScannerDetailScreen(this, chunkX, chunkZ, scans.getCompound(key)));
        } else if (!scans.contains(key) && !(jobActive && jobCx == chunkX && jobCz == chunkZ)) {
          long now = net.minecraft.Util.getMillis();
          if (chunkX == lastClickCx && chunkZ == lastClickCz && now - lastClickMs <= 350) {
            lastClickMs = 0;
            manualPending = true;
            manualCx = chunkX;
            manualCz = chunkZ;
            ModNetworking.sendToServer(new com.faktocraft.common.network.packet.PacketGeoScannerManual(
                pos, chunkX, chunkZ));
          } else {
            lastClickMs = now;
            lastClickCx = chunkX;
            lastClickCz = chunkZ;
          }
        }
      }
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (hasShiftDown()) {
      setZoom(cell + (delta > 0 ? 4 : -4));
    } else {
      pan(0, delta > 0 ? -1 : 1);
    }
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    switch (keyCode) {
      case GLFW.GLFW_KEY_LEFT -> {
        pan(-PAN_STEP, 0);
        return true;
      }
      case GLFW.GLFW_KEY_RIGHT -> {
        pan(PAN_STEP, 0);
        return true;
      }
      case GLFW.GLFW_KEY_UP -> {
        pan(0, -PAN_STEP);
        return true;
      }
      case GLFW.GLFW_KEY_DOWN -> {
        pan(0, PAN_STEP);
        return true;
      }
      default -> {
        return super.keyPressed(keyCode, scanCode, modifiers);
      }
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
