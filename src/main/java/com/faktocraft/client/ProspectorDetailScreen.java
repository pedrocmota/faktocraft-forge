package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.impl.tools.Prospector;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProspectorDetailScreen extends Screen {

  private static final int PANEL_W = 200;
  private static final int PANEL_H = 220;
  private static final int LINE_H = 11;

  private final int chunkX;
  private final int chunkZ;
  private int panelLeft;
  private int panelTop;
  private int scroll;

  public ProspectorDetailScreen(int chunkX, int chunkZ) {
    super(Component.translatable("gui." + Faktocraft.MODID + ".prospector.report", chunkX, chunkZ));
    this.chunkX = chunkX;
    this.chunkZ = chunkZ;
  }

  @Override
  protected void init() {
    panelLeft = (width - PANEL_W) / 2;
    panelTop = (height - PANEL_H) / 2;
    addRenderableWidget(Button.builder(
        Component.translatable("gui." + Faktocraft.MODID + ".prospector.back"),
        button -> minecraft.setScreen(new ProspectorScreen()))
        .bounds(panelLeft + 12, panelTop + PANEL_H - 30, PANEL_W - 24, 20)
        .build());
  }

  public static List<Component> buildReportLines(CompoundTag scan) {
    List<Component> lines = new ArrayList<>();
    CompoundTag entries = scan.getCompound("entries");
    Map<String, Integer> grouped = new HashMap<>();
    for (String original : entries.getAllKeys()) {
      int count = entries.getInt(original);
      ResourceLocation id = new ResourceLocation(original);
      String key = original;
      if (id.getPath().startsWith("deepslate_")) {
        String normalized = id.getNamespace() + ":" + id.getPath().substring("deepslate_".length());
        if (ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(normalized))) {
          key = normalized;
        }
      }
      grouped.merge(key, count, Integer::sum);
    }
    Map<Integer, List<String>> byCount = new TreeMap<>((a, b) -> b - a);
    for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
      byCount.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
    }
    for (Map.Entry<Integer, List<String>> group : byCount.entrySet()) {
      for (String id : group.getValue()) {
        Component name;
        ChatFormatting color;
        String oilKey = OIL_LABELS.get(id);
        if (oilKey != null) {
          name = Component.translatable("gui." + Faktocraft.MODID + ".prospector." + oilKey);
          color = ChatFormatting.GOLD;
        } else {
          var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
          name = block != null ? block.getName() : Component.literal(id);
          if (new ResourceLocation(id).getPath().contains("iridium")) {
            color = ChatFormatting.GOLD;
          } else {
            color = isRareOre(id) ? ChatFormatting.AQUA : ChatFormatting.WHITE;
          }
        }
        lines.add(name.copy().withStyle(color)
            .append(Component.literal(": ~" + group.getKey()).withStyle(ChatFormatting.GRAY)));
      }
    }
    return lines;
  }

  private static final Map<String, String> OIL_LABELS = Map.of(
      "faktocraft:oil_lake", "oil_lake",
      "faktocraft:oil_pocket", "oil_pocket",
      "faktocraft:oil_giant", "oil_giant",
      "faktocraft:liquid_oil", "oil_pocket");

  private static final List<String> RARE_ORES = List.of(
      "iridium", "diamond", "emerald", "ancient_debris");

  private static boolean isRareOre(String id) {
    String path = new ResourceLocation(id).getPath();
    return RARE_ORES.stream().anyMatch(path::contains);
  }

  private List<Component> currentLines() {
    ItemStack stack = ProspectorScreen.heldProspector(minecraft.player);
    if (stack.isEmpty() || minecraft.player == null) {
      return List.of();
    }
    CompoundTag scan = Prospector.getScan(stack,
        Prospector.chunkKey(minecraft.player.level(), chunkX, chunkZ));
    if (scan == null) {
      return List.of(Component.translatable("gui." + Faktocraft.MODID + ".prospector.not_scanned")
          .withStyle(ChatFormatting.DARK_GRAY));
    }
    List<Component> lines = buildReportLines(scan);
    if (lines.isEmpty()) {
      return List.of(Component.translatable("gui." + Faktocraft.MODID + ".prospector.empty")
          .withStyle(ChatFormatting.GRAY));
    }
    return lines;
  }

  private int visibleLines() {
    return (PANEL_H - 70) / LINE_H;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);

    ProspectorScreen.drawDeviceFrame(graphics, panelLeft, panelTop, PANEL_W, PANEL_H);

    graphics.drawCenteredString(font, title, panelLeft + PANEL_W / 2, panelTop + 11, 0xFFE8C43A);

    List<Component> lines = currentLines();
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
