package com.faktocraft.client;

import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScanDetailScreen extends Screen {

  private static final int PANEL_W = 200;
  private static final int PANEL_H = 220;
  private static final int LINE_H = 11;

  private static final Map<String, String> OIL_LABELS = Map.of(
      "faktocraft:oil_lake", "oil_lake",
      "faktocraft:oil_pocket", "oil_pocket",
      "faktocraft:oil_giant", "oil_giant",
      "faktocraft:liquid_oil", "oil_pocket");

  private static final List<String> RARE_ORES = List.of(
      "iridium", "diamond", "emerald", "ancient_debris");

  private final Screen parent;
  private final List<Component> lines;
  private int panelLeft;
  private int panelTop;
  private int scroll;

  public ScanDetailScreen(Screen parent, int chunkX, int chunkZ, CompoundTag scan) {
    super(Component.translatable("gui." + Faktocraft.MODID + ".prospector.report", chunkX, chunkZ));
    this.parent = parent;
    List<Component> report = buildReportLines(scan);
    this.lines = report.isEmpty()
        ? List.of(Component.translatable("gui." + Faktocraft.MODID + ".prospector.empty")
            .withStyle(ChatFormatting.GRAY))
        : report;
  }

  public static List<Component> buildReportLines(CompoundTag scan) {
    List<Component> lines = new ArrayList<>();
    CompoundTag entries = scan.getCompoundOrEmpty("entries");
    Map<String, Integer> grouped = new HashMap<>();
    for (String original : entries.keySet()) {
      grouped.merge(normalizeId(original), entries.getIntOr(original, 0), Integer::sum);
    }
    Map<Integer, List<String>> byCount = new TreeMap<>((a, b) -> b - a);
    for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
      byCount.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
    }
    for (Map.Entry<Integer, List<String>> group : byCount.entrySet()) {
      for (String id : group.getValue()) {
        lines.add(displayName(id).copy().withStyle(colorFor(id))
            .append(Component.literal(": ~" + group.getKey()).withStyle(ChatFormatting.GRAY)));
      }
    }
    return lines;
  }

  public static String normalizeId(String original) {
    Identifier id = Identifier.parse(original);
    if (id.getPath().startsWith("deepslate_")) {
      String normalized = id.getNamespace() + ":" + id.getPath().substring("deepslate_".length());
      if (BuiltInRegistries.BLOCK.containsKey(Identifier.parse(normalized))) {
        return normalized;
      }
    }
    return original;
  }

  public static Component displayName(String id) {
    String oilKey = OIL_LABELS.get(id);
    if (oilKey != null) {
      return Component.translatable("gui." + Faktocraft.MODID + ".prospector." + oilKey);
    }
    var block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
    return block != null ? block.getName() : Component.literal(id);
  }

  public static ChatFormatting colorFor(String id) {
    if (OIL_LABELS.containsKey(id) || Identifier.parse(id).getPath().contains("iridium")) {
      return ChatFormatting.GOLD;
    }
    return isRareOre(id) ? ChatFormatting.AQUA : ChatFormatting.WHITE;
  }

  private static boolean isRareOre(String id) {
    String path = Identifier.parse(id).getPath();
    return RARE_ORES.stream().anyMatch(path::contains);
  }

  @Override
  protected void init() {
    panelLeft = (width - PANEL_W) / 2;
    panelTop = (height - PANEL_H) / 2;
    addRenderableWidget(new DeviceButton(panelLeft + 12, panelTop + PANEL_H - 30, PANEL_W - 24, 20,
        Component.translatable("gui." + Faktocraft.MODID + ".prospector.back"),
        button -> minecraft.gui.setScreen(parent)));
  }

  private int visibleLines() {
    return (PANEL_H - 70) / LINE_H;
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    ScanMapScreen.drawDeviceFrame(graphics, panelLeft, panelTop, PANEL_W, PANEL_H);
    graphics.centeredText(font, title, panelLeft + PANEL_W / 2, panelTop + 11, 0xFFE8C43A);

    int maxScroll = Math.max(0, lines.size() - visibleLines());
    scroll = Mth.clamp(scroll, 0, maxScroll);
    int y = panelTop + 26;
    for (int i = scroll; i < Math.min(lines.size(), scroll + visibleLines()); i++) {
      graphics.text(font, lines.get(i), panelLeft + 14, y, GuiUtil.opaque(0xFFFFFF));
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

    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
    scroll -= (int) Math.signum(delta);
    return true;
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    int keyCode = event.key();
    if (keyCode == InputConstants.KEY_ESCAPE && minecraft != null) {
      minecraft.gui.setScreen(parent);
      return true;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean isInGameUi() {
    return true;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
