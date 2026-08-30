package com.faktocraft.client;

import com.faktocraft.IndReb;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import java.util.List;

public class GeoScannerDetailScreen extends Screen {

  private static final int PANEL_W = 200;
  private static final int PANEL_H = 220;
  private static final int LINE_H = 11;

  private final Screen parent;
  private final List<Component> lines;
  private int panelLeft;
  private int panelTop;
  private int scroll;

  public GeoScannerDetailScreen(Screen parent, int chunkX, int chunkZ, CompoundTag scan) {
    super(Component.translatable("gui." + IndReb.MODID + ".prospector.report", chunkX, chunkZ));
    this.parent = parent;
    List<Component> report = ProspectorDetailScreen.buildReportLines(scan);
    this.lines = report.isEmpty()
        ? List.of(Component.translatable("gui." + IndReb.MODID + ".prospector.empty")
            .withStyle(ChatFormatting.GRAY))
        : report;
  }

  @Override
  protected void init() {
    panelLeft = (width - PANEL_W) / 2;
    panelTop = (height - PANEL_H) / 2;
    addRenderableWidget(new DeviceButton(panelLeft + 12, panelTop + PANEL_H - 30, PANEL_W - 24, 20,
        Component.translatable("gui." + IndReb.MODID + ".prospector.back"),
        button -> minecraft.setScreen(parent)));
  }

  private int visibleLines() {
    return (PANEL_H - 70) / LINE_H;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);

    ProspectorScreen.drawDeviceFrame(graphics, panelLeft, panelTop, PANEL_W, PANEL_H);
    graphics.drawCenteredString(font, title, panelLeft + PANEL_W / 2, panelTop + 11, 0xFFE8C43A);

    int maxScroll = Math.max(0, lines.size() - visibleLines());
    scroll = Mth.clamp(scroll, 0, maxScroll);
    int y = panelTop + 26;
    for (int i = scroll; i < Math.min(lines.size(), scroll + visibleLines()); i++) {
      graphics.drawString(font, lines.get(i), panelLeft + 14, y, 0xFFFFFF);
      y += LINE_H;
    }
    if (maxScroll > 0) {
      int trackTop = panelTop + 26;
      int trackH = visibleLines() * LINE_H;
      int thumbH = Math.max(8, trackH * visibleLines() / lines.size());
      int thumbY = trackTop + (trackH - thumbH) * scroll / maxScroll;
      graphics.fill(panelLeft + PANEL_W - 8, trackTop, panelLeft + PANEL_W - 5, trackTop + trackH, 0xFF26262C);
      graphics.fill(panelLeft + PANEL_W - 8, thumbY, panelLeft + PANEL_W - 5, thumbY + thumbH, 0xFF8A8A96);
    }

    super.render(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    scroll -= (int) Math.signum(delta);
    return true;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
