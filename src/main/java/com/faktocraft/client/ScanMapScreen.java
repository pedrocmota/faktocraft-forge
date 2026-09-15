package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.scan.ScanChannel;
import com.faktocraft.common.util.GuiUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ScanMapScreen extends Screen {

  private static final int PAN_STEP = 3;

  protected static final int SIDE = 14;
  private static final int TAB_TOP = 26;
  private static final int TAB_H = 16;
  private static final int MAP_TOP = TAB_TOP + TAB_H + 6;
  private static final int BOTTOM_PAD = 18;
  private static final int ROW_H = 12;
  private static final int REPORT_HEADER_H = 16;
  private static final int FOOTER_H = 72;
  private static final int MIN_CELL = 5;
  private static final int MAX_CELL = 40;
  private static final int MIN_VISIBLE_ROWS = 6;
  private static final float MAP_ASPECT = 1.78F;

  private static final int REPORT_PAD = 6;
  private static final int REPORT_SCROLLBAR_W = 6;
  private static final int REPORT_COL_GAP = 8;
  private static final float REPORT_NAME_SCALE = 0.85F;
  private static final float REPORT_COL_SCALE = 0.75F;

  protected static final String GUI_PREFIX = "gui." + Faktocraft.MODID + ".geo_scanner.";

  protected final int centerChunkX;
  protected final int centerChunkZ;

  protected CompoundTag scans = new CompoundTag();

  private int cell = 26;
  private int gridW;
  private int gridH;

  private int viewX;
  private int viewZ;
  private boolean viewInitialized;
  private static final Map<String, int[]> SAVED_VIEWS = new HashMap<>();

  private int mapW;
  private int mapH;
  private int drawnW;
  private int drawnH;
  protected int panelW;
  protected int panelH;
  protected int panelLeft;
  protected int panelTop;

  private DynamicTexture mapTexture;
  private ResourceLocation mapLocation;
  private int pollCooldown;

  private boolean dragging;
  private double dragAccumX;
  private double dragAccumZ;
  private double dragTotal;

  private record RareFind(boolean iridium, int chunkX, int chunkZ) {
  }

  private final List<RareFind> rareFinds = new ArrayList<>();
  private int rareCycle;

  private enum Tab {
    MAP, REPORT
  }

  private record ChunkHit(int chunkX, int chunkZ, int count) {
  }

  private record Find(String id, int total, List<ChunkHit> chunks) {
  }

  protected record JobMarker(int chunkX, int chunkZ) {
  }

  private Tab tab = Tab.MAP;
  private final List<Find> finds = new ArrayList<>();
  private Find openFind;
  private int reportScroll;
  private boolean hasSelection;
  private int selectedCx;
  private int selectedCz;

  private ScanCodePopup codePopup;
  private int chipX;
  private int chipY;
  private int chipW;
  private int chipH;

  protected ScanMapScreen(Component title, int centerChunkX, int centerChunkZ) {
    super(title);
    this.centerChunkX = centerChunkX;
    this.centerChunkZ = centerChunkZ;
  }

  protected abstract int panRadius();

  protected abstract String viewKey();

  protected boolean outsideScanArea(int chunkX, int chunkZ) {
    return false;
  }

  protected boolean isHomeChunk(int chunkX, int chunkZ) {
    return chunkX == centerChunkX && chunkZ == centerChunkZ;
  }

  protected int focusChunkX() {
    return centerChunkX;
  }

  protected int focusChunkZ() {
    return centerChunkZ;
  }

  protected abstract String codeText();

  protected abstract void applyCode(int value);

  protected abstract void poll();

  protected abstract int minMapWidth();

  protected abstract void addPrimaryButton(int left, int top, int width);

  protected abstract void renderFooter(GuiGraphics graphics, int footerTop);

  @Nullable
  protected abstract JobMarker activeJob();

  protected boolean isPendingChunk(int chunkX, int chunkZ) {
    return false;
  }

  protected void addUnscannedHint(List<Component> lines) {
  }

  protected void onUnscannedClicked(int chunkX, int chunkZ) {
  }

  protected boolean stillValid() {
    return true;
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

  protected static boolean isIridium(String id) {
    return id.contains("iridium");
  }

  protected static boolean isGiantOil(String id) {
    return id.equals("faktocraft:oil_giant");
  }

  protected void setScans(CompoundTag newScans) {
    scans = newScans;
    rebuildRareFinds();
    rebuildFinds();
  }

  protected void requestPoll() {
    pollCooldown = 2;
  }

  @Nullable
  protected CompoundTag scanAt(int chunkX, int chunkZ) {
    String key = ScanChannel.localKey(chunkX, chunkZ);
    return scans.contains(key) ? scans.getCompound(key) : null;
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

  private void rebuildFinds() {
    Map<String, Integer> totals = new HashMap<>();
    Map<String, List<ChunkHit>> hits = new HashMap<>();
    for (String key : scans.getAllKeys()) {
      CompoundTag entries = scans.getCompound(key).getCompound("entries");
      String[] parts = key.split(",");
      int cx = Integer.parseInt(parts[0]);
      int cz = Integer.parseInt(parts[1]);
      Map<String, Integer> perChunk = new HashMap<>();
      for (String id : entries.getAllKeys()) {
        perChunk.merge(ScanDetailScreen.normalizeId(id), entries.getInt(id), Integer::sum);
      }
      for (Map.Entry<String, Integer> entry : perChunk.entrySet()) {
        totals.merge(entry.getKey(), entry.getValue(), Integer::sum);
        hits.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
            .add(new ChunkHit(cx, cz, entry.getValue()));
      }
    }
    finds.clear();
    for (Map.Entry<String, Integer> entry : totals.entrySet()) {
      List<ChunkHit> chunks = hits.get(entry.getKey());
      chunks.sort((a, b) -> Integer.compare(b.count(), a.count()));
      finds.add(new Find(entry.getKey(), entry.getValue(), chunks));
    }
    finds.sort((a, b) -> Integer.compare(b.total(), a.total()));
    if (openFind != null) {
      String openId = openFind.id();
      openFind = finds.stream().filter(f -> f.id().equals(openId)).findFirst().orElse(null);
    }
  }

  private void selectChunk(int chunkX, int chunkZ) {
    hasSelection = true;
    selectedCx = chunkX;
    selectedCz = chunkZ;
    viewX = (chunkX - centerChunkX) - gridW / 2;
    viewZ = (chunkZ - centerChunkZ) - gridH / 2;
    clampView();
    buildSurfaceMap();
  }

  protected void centerView() {
    viewX = (focusChunkX() - centerChunkX) - gridW / 2;
    viewZ = (focusChunkZ() - centerChunkZ) - gridH / 2;
    clampView();
    buildSurfaceMap();
  }

  private boolean goBack() {
    if (tab == Tab.REPORT && openFind != null) {
      openFind = null;
      reportScroll = 0;
      return true;
    }
    return false;
  }

  private int mapLeft() {
    return panelLeft + SIDE + (mapW - drawnW) / 2;
  }

  private int mapTop() {
    return panelTop + MAP_TOP + (mapH - drawnH) / 2;
  }

  @Override
  protected void init() {
    int titleW = font.width(title) + 12;
    int centerW = font.width(Component.translatable(GUI_PREFIX + "center"));
    int textNeed = Math.max(Math.max(minMapWidth(), titleW), 2 * (centerW + 18) + 8);

    mapH = Math.max(7 * MIN_CELL, height - MAP_TOP - FOOTER_H - BOTTOM_PAD - 16);
    mapW = Math.max(textNeed, Math.min((int) (mapH * MAP_ASPECT), width - 2 * SIDE - 8));

    panelW = mapW + 2 * SIDE;
    panelH = MAP_TOP + mapH + FOOTER_H + BOTTOM_PAD;
    panelLeft = (width - panelW) / 2;
    panelTop = (height - panelH) / 2;

    recomputeViewport(true);

    int half = (panelW - 2 * SIDE - 8) / 2;
    int buttonsY = panelTop + panelH - BOTTOM_PAD - 20;
    addPrimaryButton(panelLeft + SIDE, buttonsY, half);
    addRenderableWidget(new DeviceButton(panelLeft + SIDE + half + 8, buttonsY, half, 20,
        Component.translatable(GUI_PREFIX + "center"), button -> centerView()));

    codePopup = new ScanCodePopup(this, font, this::codeText, this::applyCode);
    addWidget(codePopup.box());
    addWidget(codePopup.button());
  }

  private int maxCell() {
    return Math.max(MIN_CELL, Math.min(MAX_CELL, mapH / MIN_VISIBLE_ROWS));
  }

  private void recomputeViewport(boolean keepCenter) {
    int oldCenterX = viewX + gridW / 2;
    int oldCenterZ = viewZ + gridH / 2;
    int[] saved = viewInitialized ? null : SAVED_VIEWS.get(viewKey());
    if (saved != null) {
      cell = saved[0];
    }
    cell = Mth.clamp(cell, MIN_CELL, maxCell());

    int span = 2 * panRadius() + 1;
    gridW = Mth.clamp((mapW + cell - 1) / cell, 1, span);
    gridH = Mth.clamp((mapH + cell - 1) / cell, 1, span);
    drawnW = Math.min(mapW, gridW * cell);
    drawnH = Math.min(mapH, gridH * cell);
    if (!viewInitialized) {
      viewInitialized = true;
      int startX = saved != null ? saved[1] - centerChunkX : 0;
      int startZ = saved != null ? saved[2] - centerChunkZ : 0;
      viewX = startX - gridW / 2;
      viewZ = startZ - gridH / 2;
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
      poll();
    }
    if (codePopup != null) {
      codePopup.tick();
    }
  }

  private void clampView() {
    int radius = panRadius();
    viewX = Mth.clamp(viewX, -radius, radius - (gridW - 1));
    viewZ = Mth.clamp(viewZ, -radius, radius - (gridH - 1));
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
    mapLocation = minecraft.getTextureManager().register("scan_map", mapTexture);
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
    if (viewInitialized) {
      SAVED_VIEWS.put(viewKey(),
          new int[] { cell, centerChunkX + viewX + gridW / 2, centerChunkZ + viewZ + gridH / 2 });
    }
    closeMapTexture();
    super.removed();
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!stillValid()) {
      onClose();
      return;
    }
    renderBackground(graphics);

    drawDeviceFrame(graphics, panelLeft, panelTop, panelW, panelH);
    graphics.drawCenteredString(font, title, panelLeft + panelW / 2, panelTop + 12, 0xFFE8C43A);

    drawTabs(graphics, mouseX, mouseY);

    List<Component> hoverLines;
    if (tab == Tab.REPORT) {
      hoverLines = renderReport(graphics, mouseX, mouseY);
    } else {
      hoverLines = renderMap(graphics, mouseX, mouseY, partialTick);
    }

    renderFooter(graphics, panelTop + MAP_TOP + mapH);

    codePopup.setIcon(panelLeft + panelW - SIDE - ScanCodePopup.ICON_SIZE, panelTop + 9);
    codePopup.render(graphics, mouseX, mouseY);

    super.render(graphics, mouseX, mouseY, partialTick);

    if (codePopup.isOpen()) {
      codePopup.renderOverlay(graphics, mouseX, mouseY, partialTick);
      return;
    }
    codePopup.renderTooltip(graphics, mouseX, mouseY);
    if (hoverLines != null && !dragging) {
      graphics.renderComponentTooltip(font, hoverLines, mouseX, mouseY);
    }
  }

  private int tabWidth() {
    return (mapW - 4) / 2;
  }

  private int tabLeft(Tab which) {
    return panelLeft + SIDE + which.ordinal() * (tabWidth() + 4);
  }

  private Component tabLabel(Tab which) {
    return switch (which) {
      case MAP -> Component.translatable(GUI_PREFIX + "tab_map");
      case REPORT -> finds.isEmpty() ? Component.translatable(GUI_PREFIX + "tab_report")
          : Component.translatable(GUI_PREFIX + "tab_report").append(Component.literal(" (" + finds.size() + ")"));
    };
  }

  private Tab tabAt(double mouseX, double mouseY) {
    int top = panelTop + TAB_TOP;
    if (mouseY < top || mouseY >= top + TAB_H) {
      return null;
    }
    for (Tab which : Tab.values()) {
      int left = tabLeft(which);
      if (mouseX >= left && mouseX < left + tabWidth()) {
        return which;
      }
    }
    return null;
  }

  private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
    Tab hovered = tabAt(mouseX, mouseY);
    int top = panelTop + TAB_TOP;
    for (Tab which : Tab.values()) {
      int left = tabLeft(which);
      int right = left + tabWidth();
      boolean active = which == tab;
      boolean hover = which == hovered;
      int fill = active ? 0xFF262A30 : hover ? 0xFF1E2126 : 0xFF17191D;
      int border = active ? 0xFFE8C43A : hover ? 0xFF9AA0A8 : 0xFF3A3E44;
      int text = active ? 0xFFE8C43A : hover ? 0xFFE8E8E8 : 0xFF8A8E94;
      graphics.fill(left, top, right, top + TAB_H, fill);
      graphics.fill(left, top, right, top + 1, border);
      graphics.fill(left, top, left + 1, top + TAB_H, border);
      graphics.fill(right - 1, top, right, top + TAB_H, border);
      if (!active) {
        graphics.fill(left, top + TAB_H - 1, right, top + TAB_H, 0xFFE8C43A);
      }
      String label = tabLabel(which).getString();
      int labelW = font.width(label);
      int maxW = tabWidth() - 8;
      float scale = labelW <= maxW ? 1.0F : (float) maxW / labelW;
      int drawX = (left + right) / 2 - Math.round(labelW * scale / 2);
      GuiUtil.renderScaled(graphics, label, drawX, top + 4, scale, text, true);
    }
  }

  private int reportRows() {
    return Math.max(1, (drawnH - REPORT_HEADER_H) / ROW_H);
  }

  private int reportRowCount() {
    return openFind == null ? finds.size() : openFind.chunks().size();
  }

  private int reportRowAt(double mouseX, double mouseY) {
    int gridLeft = mapLeft();
    int gridTop = mapTop() + REPORT_HEADER_H;
    if (mouseX < gridLeft || mouseX >= gridLeft + drawnW || mouseY < gridTop) {
      return -1;
    }
    int row = reportScroll + (int) ((mouseY - gridTop) / ROW_H);
    if (row - reportScroll >= reportRows() || row >= reportRowCount()) {
      return -1;
    }
    return row;
  }

  private boolean overReportHeader(double mouseX, double mouseY) {
    int gridLeft = mapLeft();
    int gridTop = mapTop();
    return mouseX >= gridLeft && mouseX < gridLeft + drawnW && mouseY >= gridTop
        && mouseY < gridTop + REPORT_HEADER_H;
  }

  private void drawScrolling(GuiGraphics graphics, Component text, int x, int y, int maxWidth) {
    if (maxWidth <= 0) {
      return;
    }
    int textWidth = Math.round(font.width(text) * REPORT_NAME_SCALE);
    int offset = 0;
    if (textWidth > maxWidth) {
      double overflow = textWidth - maxWidth;
      double seconds = net.minecraft.Util.getMillis() / 1000.0;
      double period = Math.max(overflow * 0.5, 3.0);
      double swing = Math.sin(Math.PI / 2 * Math.cos(2 * Math.PI * seconds / period)) / 2 + 0.5;
      offset = (int) Math.round(Mth.lerp(swing, 0.0, overflow));
      graphics.enableScissor(x, y - 1, x + maxWidth, y + 10);
    }
    graphics.pose().pushPose();
    graphics.pose().translate(x - offset, y + 4 - REPORT_NAME_SCALE * 4, 0);
    graphics.pose().scale(REPORT_NAME_SCALE, REPORT_NAME_SCALE, 1.0F);
    graphics.drawString(font, text, 0, 0, 0xFFFFFF);
    graphics.pose().popPose();
    if (textWidth > maxWidth) {
      graphics.disableScissor();
    }
  }

  private int scaledWidth(Component text) {
    return Math.round(font.width(text) * REPORT_COL_SCALE);
  }

  private void drawRight(GuiGraphics graphics, Component text, int right, int y) {
    int color = text.getStyle().getColor() != null ? text.getStyle().getColor().getValue() : 0xFFFFFF;
    GuiUtil.renderScaled(graphics, text.getString(), right - scaledWidth(text), y, REPORT_COL_SCALE,
        0xFF000000 | color, true);
  }

  private Component countText(int value) {
    return Component.literal("~" + value).withStyle(ChatFormatting.GRAY);
  }

  private Component chunksText(int value) {
    return Component.translatable(GUI_PREFIX + "report_chunks", value).withStyle(ChatFormatting.DARK_GRAY);
  }

  private List<Component> renderReport(GuiGraphics graphics, int mouseX, int mouseY) {
    int gridLeft = mapLeft();
    int gridTop = mapTop();
    graphics.fill(gridLeft, gridTop, gridLeft + drawnW, gridTop + drawnH, 0xFF14161A);

    List<Component> hoverLines = null;

    int rows = reportRows();
    int count = reportRowCount();
    int maxScroll = Math.max(0, count - rows);
    reportScroll = Mth.clamp(reportScroll, 0, maxScroll);
    int hoveredRow = reportRowAt(mouseX, mouseY);

    int left = gridLeft + REPORT_PAD;
    int right = gridLeft + drawnW - REPORT_PAD - (maxScroll > 0 ? REPORT_SCROLLBAR_W : 0);

    int countColW = 0;
    int chunksColW = 0;
    if (openFind == null) {
      for (Find find : finds) {
        countColW = Math.max(countColW, scaledWidth(countText(find.total())));
        chunksColW = Math.max(chunksColW, scaledWidth(chunksText(find.chunks().size())));
      }
    } else {
      for (ChunkHit hit : openFind.chunks()) {
        countColW = Math.max(countColW, scaledWidth(countText(hit.count())));
      }
    }
    int chunksRight = right;
    int countRight = chunksColW > 0 ? chunksRight - chunksColW - REPORT_COL_GAP : right;
    int nameMax = countRight - countColW - REPORT_COL_GAP - left;

    if (openFind == null) {
      Component header = finds.isEmpty()
          ? Component.translatable(GUI_PREFIX + "report_empty").withStyle(ChatFormatting.DARK_GRAY)
          : Component.translatable(GUI_PREFIX + "report_summary", finds.size(), scans.getAllKeys().size())
              .withStyle(ChatFormatting.GRAY);
      drawScrolling(graphics, header, left, gridTop + 4, gridLeft + drawnW - REPORT_PAD - left);
    } else {
      boolean overBack = overReportHeader(mouseX, mouseY);
      graphics.fill(gridLeft, gridTop, gridLeft + drawnW, gridTop + REPORT_HEADER_H,
          overBack ? 0xFF262A30 : 0xFF1E2126);
      Component back = Component.translatable(GUI_PREFIX + "report_back")
          .withStyle(overBack ? ChatFormatting.GOLD : ChatFormatting.GRAY);
      graphics.drawString(font, back, left, gridTop + 4, 0xFFFFFF);
      Component total = countText(openFind.total());
      drawRight(graphics, total, right, gridTop + 4);
      int nameLeft = left + font.width(back) + REPORT_COL_GAP;
      Component name = ScanDetailScreen.displayName(openFind.id()).copy()
          .withStyle(ScanDetailScreen.colorFor(openFind.id()));
      drawScrolling(graphics, name, nameLeft, gridTop + 4, right - scaledWidth(total) - REPORT_COL_GAP - nameLeft);
    }
    graphics.fill(gridLeft, gridTop + REPORT_HEADER_H - 1, gridLeft + drawnW, gridTop + REPORT_HEADER_H,
        0xFF32363C);

    graphics.enableScissor(gridLeft, gridTop + REPORT_HEADER_H, gridLeft + drawnW, gridTop + drawnH);
    int y = gridTop + REPORT_HEADER_H;
    for (int i = reportScroll; i < Math.min(count, reportScroll + rows); i++) {
      boolean hover = i == hoveredRow;
      if (hover) {
        graphics.fill(gridLeft, y, gridLeft + drawnW, y + ROW_H, 0x28FFFFFF);
      } else if ((i & 1) == 1) {
        graphics.fill(gridLeft, y, gridLeft + drawnW, y + ROW_H, 0x18FFFFFF);
      }
      Component name;
      if (openFind == null) {
        Find find = finds.get(i);
        name = ScanDetailScreen.displayName(find.id()).copy().withStyle(ScanDetailScreen.colorFor(find.id()));
        drawRight(graphics, countText(find.total()), countRight, y + 2);
        drawRight(graphics, chunksText(find.chunks().size()), chunksRight, y + 2);
      } else {
        ChunkHit hit = openFind.chunks().get(i);
        boolean selected = hasSelection && hit.chunkX() == selectedCx && hit.chunkZ() == selectedCz;
        name = Component.literal("Chunk " + hit.chunkX() + ", " + hit.chunkZ())
            .withStyle(selected ? ChatFormatting.AQUA : ChatFormatting.WHITE);
        drawRight(graphics, countText(hit.count()), countRight, y + 2);
      }
      drawScrolling(graphics, name, left, y + 2, nameMax);
      y += ROW_H;
    }
    graphics.disableScissor();

    if (maxScroll > 0) {
      int trackTop = gridTop + REPORT_HEADER_H;
      int trackH = rows * ROW_H;
      int thumbH = Math.max(8, trackH * rows / count);
      int thumbY = trackTop + (trackH - thumbH) * reportScroll / maxScroll;
      graphics.fill(gridLeft + drawnW - 4, trackTop, gridLeft + drawnW - 1, trackTop + trackH, 0xFF26262C);
      graphics.fill(gridLeft + drawnW - 4, thumbY, gridLeft + drawnW - 1, thumbY + thumbH, 0xFF8A8A96);
    }

    if (hoveredRow >= 0) {
      hoverLines = List.of(Component.translatable(
          GUI_PREFIX + (openFind == null ? "report_find_hint" : "report_chunk_hint"))
          .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
    return hoverLines;
  }

  private List<Component> renderMap(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
    JobMarker job = activeJob();

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
        CompoundTag scan = scanAt(chunkX, chunkZ);

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

        if (hasSelection && selectedCx == chunkX && selectedCz == chunkZ) {
          float pulse = 0.6F + 0.4F * Mth.sin((tick + partialTick) * 0.35F);
          int alpha = (int) (pulse * 255) << 24;
          drawCellBorder(graphics, cx, cy, 0xFFFFFFFF);
          graphics.fill(cx + 1, cy + 1, cx + cell - 1, cy + 2, alpha | 0x4FC3F7);
          graphics.fill(cx + 1, cy + cell - 2, cx + cell - 1, cy + cell - 1, alpha | 0x4FC3F7);
          graphics.fill(cx + 1, cy + 1, cx + 2, cy + cell - 1, alpha | 0x4FC3F7);
          graphics.fill(cx + cell - 2, cy + 1, cx + cell - 1, cy + cell - 1, alpha | 0x4FC3F7);
        } else if (job != null && job.chunkX() == chunkX && job.chunkZ() == chunkZ) {
          float pulse = 0.55F + 0.45F * Mth.sin((tick + partialTick) * 0.35F);
          int alpha = (int) (pulse * 255) << 24;
          drawCellBorder(graphics, cx, cy, alpha | 0xE8C43A);
        } else if (isPendingChunk(chunkX, chunkZ)) {
          float pulse = 0.55F + 0.45F * Mth.sin((tick + partialTick) * 0.35F);
          int alpha = (int) (pulse * 255) << 24;
          drawCellBorder(graphics, cx, cy, alpha | 0x4FC3F7);
        } else if (isHomeChunk(chunkX, chunkZ)) {
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
    drawPlayerMarker(graphics, gridLeft, gridTop);
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
        lines.add(Component.translatable(GUI_PREFIX + "rare_header").withStyle(ChatFormatting.GOLD));
        for (RareFind find : rareFinds) {
          Component name = find.iridium()
              ? Component.translatable("item." + Faktocraft.MODID + ".iridium")
              : Component.translatable("gui." + Faktocraft.MODID + ".prospector.oil_giant");
          lines.add(name.copy().withStyle(find.iridium() ? ChatFormatting.GOLD : ChatFormatting.YELLOW)
              .append(Component.literal(" · " + find.chunkX() + ", " + find.chunkZ())
                  .withStyle(ChatFormatting.GRAY)));
        }
        lines.add(Component.translatable(GUI_PREFIX + "rare_hint")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        hoverLines = lines;
      }
    } else {
      chipW = 0;
      chipH = 0;
    }
    return hoverLines;
  }

  private void drawPlayerMarker(GuiGraphics graphics, int gridLeft, int gridTop) {
    if (minecraft == null || minecraft.player == null) {
      return;
    }
    int gx = minecraft.player.chunkPosition().x - centerChunkX - viewX;
    int gz = minecraft.player.chunkPosition().z - centerChunkZ - viewZ;
    if (gx < 0 || gz < 0 || gx >= gridW || gz >= gridH) {
      return;
    }
    int size = Mth.clamp(cell / 2, 8, 16);
    int x = gridLeft + gx * cell + (cell - size) / 2;
    int y = gridTop + gz * cell + (cell - size) / 2;
    graphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF14161A);
    PlayerFaceRenderer.draw(graphics, minecraft.player.getSkinTextureLocation(), x, y, size);
  }

  protected void drawProgressBar(GuiGraphics graphics, int barTop, float progress, boolean active) {
    int barLeft = panelLeft + SIDE;
    int barW = panelW - 2 * SIDE;
    graphics.fill(barLeft, barTop, barLeft + barW, barTop + 8, 0xFF26262C);
    if (active) {
      graphics.fill(barLeft + 1, barTop + 1, barLeft + 1 + (int) ((barW - 2) * progress), barTop + 7, 0xFF7F9E32);
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

  private List<Component> buildHoverSummary(int chunkX, int chunkZ, @Nullable CompoundTag scan) {
    List<Component> lines = new ArrayList<>();
    lines.add(Component.literal("Chunk " + chunkX + ", " + chunkZ).withStyle(ChatFormatting.WHITE));
    if (scan == null) {
      lines.add(Component.translatable("gui." + Faktocraft.MODID + ".prospector.not_scanned")
          .withStyle(ChatFormatting.DARK_GRAY));
      addUnscannedHint(lines);
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
    lines.add(Component.translatable(GUI_PREFIX + "pan_hint").withStyle(ChatFormatting.DARK_GRAY,
        ChatFormatting.ITALIC));
    return lines;
  }

  private boolean overMap(double mouseX, double mouseY) {
    int gridLeft = mapLeft();
    int gridTop = mapTop();
    return mouseX >= gridLeft && mouseX < gridLeft + drawnW && mouseY >= gridTop && mouseY < gridTop + drawnH;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (codePopup.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    if (button == 0) {
      Tab clicked = tabAt(mouseX, mouseY);
      if (clicked != null) {
        if (clicked != tab) {
          tab = clicked;
          reportScroll = 0;
        }
        return true;
      }
    }
    if (tab == Tab.REPORT) {
      if (button == 0) {
        if (openFind != null && overReportHeader(mouseX, mouseY)) {
          goBack();
          return true;
        }
        int row = reportRowAt(mouseX, mouseY);
        if (row >= 0) {
          if (openFind == null) {
            openFind = finds.get(row);
            reportScroll = 0;
          } else {
            ChunkHit hit = openFind.chunks().get(row);
            selectChunk(hit.chunkX(), hit.chunkZ());
            tab = Tab.MAP;
          }
          return true;
        }
      } else if (button == 1 && goBack()) {
        return true;
      }
      return false;
    }
    if (button == 0 && chipW > 0 && mouseX >= chipX && mouseX < chipX + chipW
        && mouseY >= chipY && mouseY < chipY + chipH) {
      RareFind find = rareFinds.get(rareCycle % rareFinds.size());
      rareCycle++;
      selectChunk(find.chunkX(), find.chunkZ());
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
        CompoundTag scan = scanAt(chunkX, chunkZ);
        if (scan != null && minecraft != null) {
          minecraft.setScreen(new ScanDetailScreen(this, chunkX, chunkZ, scan));
        } else if (scan == null) {
          onUnscannedClicked(chunkX, chunkZ);
        }
      }
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (tab == Tab.REPORT) {
      reportScroll -= (int) Math.signum(delta);
      return true;
    }
    if (hasShiftDown()) {
      setZoom(cell + (delta > 0 ? 4 : -4));
    } else {
      pan(0, delta > 0 ? -1 : 1);
    }
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (codePopup.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    }
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      if (!goBack()) {
        onClose();
      }
      return true;
    }
    if (tab == Tab.REPORT) {
      if (keyCode == GLFW.GLFW_KEY_UP) {
        reportScroll--;
        return true;
      }
      if (keyCode == GLFW.GLFW_KEY_DOWN) {
        reportScroll++;
        return true;
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
    }
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
  public boolean shouldCloseOnEsc() {
    return false;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
