package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.registries.BuiltInRegistries;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketModuleTree;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ModuleTreePanel {

  public static final int X = 8;
  public static final int Y = 30;
  public static final int W = 221;
  public static final int ROWS = 8;
  public static final int ROW_H = 14;

  private static final int FRAME_FILL = 0xFF8B8B8B;
  private static final int FRAME_DARK = 0xFF373737;
  private static final int FRAME_LIGHT = 0xFFFFFFFF;
  private static final int BAR_TRACK = 0xFF6E6E6E;
  private static final int BAR_THUMB = 0xFF3A3A3A;
  private static final int TEXT = 0x2E2E2E;
  private static final int ALLOW = 0xFF1E9E38;
  private static final int DENY = 0xFFC03030;
  private static final int MIXED = 0xFFC8A414;

  private static final byte STATE_DENY = 0;
  private static final byte STATE_ALLOW = 1;
  private static final byte STATE_MIXED = 2;

  private record Row(String node, int depth, Component label, @Nullable Item item, boolean leaf, byte state,
      int count) {
  }

  private final MenuModule menu;
  private final Font font;
  private final Set<String> expanded = new HashSet<>();
  private final List<Row> rows = new ArrayList<>();
  private int scroll;
  private String query = "";
  @Nullable
  private CompoundTag builtTag;

  @Nullable
  private String editingNode;

  @Nullable
  private String armedNode;
  private long armedAt;
  private boolean draggingBar;
  private double dragGrab;

  public record CountField(String node, int x, int y, int width, int count) {
  }

  private static final int COUNT_W = 34;
  private static final int BAR_W = 6;

  private static final long DOUBLE_CLICK_MS = 250L;

  public ModuleTreePanel(MenuModule menu, Font font) {
    this.menu = menu;
    this.font = font;
  }

  private String key(String name) {
    return "logistics." + Faktocraft.MODID + "." + name;
  }

  public void setQuery(String query) {
    if (!this.query.equals(query)) {
      this.query = query;
      scroll = 0;
      armedNode = null;
      rebuild();
    }
  }

  private byte leafState(Map<String, Boolean> overrides, Item item) {
    return LogisticsItemTree.allows(overrides, new ItemStack(item)) ? STATE_ALLOW : STATE_DENY;
  }

  private boolean isSupplier() {
    return menu.getModuleType() == ModuleType.SUPPLIER;
  }

  private static byte merge(int allowCount, int total) {
    if (allowCount == 0) {
      return STATE_DENY;
    }
    return allowCount == total ? STATE_ALLOW : STATE_MIXED;
  }

  private static Component tabLabel(ResourceLocation tabId) {
    CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(tabId);
    return tab != null ? tab.getDisplayName() : Component.literal(tabId.toString());
  }

  private static Component modLabel(String namespace) {
    return Component.literal(ModList.get().getModContainerById(namespace)
        .map(container -> container.getModInfo().getDisplayName())
        .orElse(namespace));
  }

  private static List<String> orderedNamespaces(Set<String> present) {
    List<String> order = new ArrayList<>();
    if (present.contains(Faktocraft.MODID)) {
      order.add(Faktocraft.MODID);
    }
    if (present.contains("minecraft")) {
      order.add("minecraft");
    }
    present.stream().filter(ns -> !order.contains(ns)).sorted().forEach(order::add);
    return order;
  }

  public void rebuildIfChanged() {
    if (!Objects.equals(menu.getModuleStack().getTag(), builtTag)) {
      rebuild();
    }
  }

  public void rebuild() {
    rows.clear();
    CompoundTag currentTag = menu.getModuleStack().getTag();
    builtTag = currentTag != null ? currentTag.copy() : null;
    Map<String, Boolean> overrides = ModuleSettings.treeOverrides(menu.getModuleStack());
    Map<String, Integer> counts = isSupplier()
        ? ModuleSettings.treeCounts(menu.getModuleStack())
        : Map.of();
    Map<String, Map<ResourceLocation, List<Item>>> index = LogisticsItemTree.byNamespace();
    String needle = query.trim().toLowerCase(Locale.ROOT);
    boolean searching = !needle.isEmpty();
    for (String ns : orderedNamespaces(index.keySet())) {
      Map<ResourceLocation, List<Item>> tabs = index.get(ns);
      int nsRowIndex = rows.size();
      int nsAllow = 0;
      int nsTotal = 0;
      boolean nsVisible = false;
      boolean nsExpanded = searching || expanded.contains(LogisticsItemTree.namespaceNode(ns));
      for (Map.Entry<ResourceLocation, List<Item>> tab : tabs.entrySet()) {
        String catNode = LogisticsItemTree.categoryNode(ns, tab.getKey());
        boolean catMatch = searching
            && tabLabel(tab.getKey()).getString().toLowerCase(Locale.ROOT).contains(needle);
        int catRowIndex = rows.size();
        int catAllow = 0;
        int catVisible = 0;
        boolean catExpanded = searching || expanded.contains(catNode);
        if (nsExpanded) {
          rows.add(null);
        }
        for (Item item : tab.getValue()) {
          byte state = leafState(overrides, item);
          nsTotal++;
          catAllow += state == STATE_ALLOW ? 1 : 0;
          nsAllow += state == STATE_ALLOW ? 1 : 0;
          ItemStack stack = new ItemStack(item);
          boolean match = !searching || catMatch
              || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle);
          if (!match) {
            continue;
          }
          catVisible++;
          if (nsExpanded && catExpanded) {
            String itemNode = LogisticsItemTree.itemNode(item);
            rows.add(new Row(itemNode, 2, stack.getHoverName(), item, true, state,
                counts.getOrDefault(itemNode, 0)));
          }
        }
        if (searching && catVisible == 0) {
          if (nsExpanded) {
            rows.remove(catRowIndex);
          }
          continue;
        }
        nsVisible = nsVisible || !searching || catVisible > 0;
        if (nsExpanded) {
          rows.set(catRowIndex, new Row(catNode, 1, tabLabel(tab.getKey()), null, false,
              merge(catAllow, tab.getValue().size()), 0));
        }
      }
      if (searching && !nsVisible) {
        while (rows.size() > nsRowIndex) {
          rows.remove(rows.size() - 1);
        }
        continue;
      }
      rows.add(nsRowIndex, new Row(LogisticsItemTree.namespaceNode(ns), 0, modLabel(ns), null, false,
          merge(nsAllow, nsTotal), 0));
    }
    int maxScroll = Math.max(0, rows.size() - ROWS);
    if (scroll > maxScroll) {
      scroll = maxScroll;
    }
  }

  public void render(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
    int x0 = left + X;
    int y0 = top + Y;
    if (!hasScrollBar()) {
      draggingBar = false;
    }
    graphics.fill(x0 - 1, y0 - 1, x0 + W + 1, y0 + ROWS * ROW_H + 1, FRAME_FILL);
    graphics.fill(x0 - 1, y0 - 1, x0 + W + 1, y0, FRAME_DARK);
    graphics.fill(x0 - 1, y0 - 1, x0, y0 + ROWS * ROW_H + 1, FRAME_DARK);
    graphics.fill(x0 - 1, y0 + ROWS * ROW_H, x0 + W + 1, y0 + ROWS * ROW_H + 1, FRAME_LIGHT);
    graphics.fill(x0 + W, y0 - 1, x0 + W + 1, y0 + ROWS * ROW_H + 1, FRAME_LIGHT);

    if (rows.isEmpty()) {
      Component empty = Component.translatable(key("tree.empty"));
      graphics.drawString(font, empty, x0 + (W - font.width(empty)) / 2, y0 + (ROWS * ROW_H - 8) / 2,
          0x555555, false);
      return;
    }
    for (int i = 0; i < ROWS; i++) {
      int rowIndex = scroll + i;
      if (rowIndex >= rows.size()) {
        break;
      }
      drawRow(graphics, rows.get(rowIndex), x0, y0 + i * ROW_H, mouseX, mouseY);
    }
    if (hasScrollBar()) {
      int barX = x0 + W - BAR_W + 1;
      graphics.fill(barX, y0 + 1, barX + 4, y0 + 1 + trackHeight(), BAR_TRACK);
      graphics.fill(barX, thumbY(top), barX + 4, thumbY(top) + thumbHeight(),
          draggingBar ? FRAME_DARK : BAR_THUMB);
    }
  }

  private static int rowIndent(Row row, int x0) {
    return x0 + 3 + row.depth() * 10;
  }

  private static int iconX(Row row, int x0) {
    return rowIndent(row, x0) + (row.leaf() ? 0 : 9);
  }

  private boolean hasScrollBar() {
    return rows.size() > ROWS;
  }

  private int trackHeight() {
    return ROWS * ROW_H - 2;
  }

  private int thumbHeight() {
    return Math.max(8, trackHeight() * ROWS / Math.max(1, rows.size()));
  }

  private int thumbY(int top) {
    int span = trackHeight() - thumbHeight();
    int maxScroll = Math.max(1, rows.size() - ROWS);
    return top + Y + 1 + span * scroll / maxScroll;
  }

  private void drawRow(GuiGraphics graphics, Row row, int x0, int y, int mouseX, int mouseY) {
    boolean hovered = mouseX >= x0 && mouseX < x0 + W && mouseY >= y && mouseY < y + ROW_H;
    if (hovered) {
      graphics.fill(x0, y, x0 + W, y + ROW_H, 0x30FFFFFF);
    }
    int x = x0 + 3 + row.depth() * 10;
    if (!row.leaf()) {
      drawTriangle(graphics, x, y + 3, expanded.contains(row.node()) && query.trim().isEmpty()
          || !query.trim().isEmpty());
      x += 9;
    }
    if (row.item() != null) {
      graphics.pose().pushPose();
      graphics.pose().translate(x, y + 1, 0);
      graphics.pose().scale(0.75f, 0.75f, 1);
      graphics.renderItem(new ItemStack(row.item()), 0, 0);
      graphics.pose().popPose();
      x += 15;
    }
    boolean showCount = row.leaf() && isSupplier() && row.state() == STATE_ALLOW;
    int textLimit = x0 + W - (showCount ? 22 + COUNT_W + 4 : 22) - x;
    graphics.drawString(font,
        net.minecraft.locale.Language.getInstance().getVisualOrder(font.substrByWidth(row.label(), textLimit)),
        x, y + 3, TEXT, false);
    if (showCount) {
      int fieldX = x0 + W - 22 - COUNT_W;
      graphics.fill(fieldX, y + 1, fieldX + COUNT_W, y + ROW_H - 2, FRAME_DARK);
      graphics.fill(fieldX + 1, y + 2, fieldX + COUNT_W - 1, y + ROW_H - 3, 0xFF1E1E1E);
      if (!row.node().equals(editingNode)) {
        String count = String.valueOf(row.count());
        graphics.drawString(font, count, fieldX + COUNT_W - 3 - font.width(count), y + 3, 0xE0E0E0, false);
      }
    }
    drawState(graphics, x0 + W - 17, y + 3, row.state());
  }

  private void drawTriangle(GuiGraphics graphics, int x, int y, boolean open) {
    if (open) {
      for (int i = 0; i < 4; i++) {
        graphics.fill(x + i, y + i, x + 7 - i, y + i + 1, FRAME_DARK);
      }
    } else {
      for (int i = 0; i < 4; i++) {
        graphics.fill(x + i, y + i, x + i + 1, y + 7 - i, FRAME_DARK);
      }
    }
  }

  private void drawState(GuiGraphics graphics, int x, int y, byte state) {
    if (state == STATE_ALLOW) {
      for (int i = 0; i < 3; i++) {
        graphics.fill(x + i, y + 3 + i, x + i + 1, y + 5 + i, ALLOW);
      }
      for (int i = 0; i < 5; i++) {
        graphics.fill(x + 3 + i, y + 4 - i, x + 4 + i, y + 6 - i, ALLOW);
      }
    } else if (state == STATE_DENY) {
      for (int i = 0; i < 7; i++) {
        graphics.fill(x + i, y + i, x + i + 1, y + i + 1, DENY);
        graphics.fill(x + 6 - i, y + i, x + 7 - i, y + i + 1, DENY);
      }
    } else {
      graphics.fill(x, y + 3, x + 2, y + 5, MIXED);
      graphics.fill(x + 2, y + 2, x + 5, y + 4, MIXED);
      graphics.fill(x + 5, y + 3, x + 7, y + 5, MIXED);
    }
  }

  public void renderTooltip(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
    Row row = rowAt(left, top, mouseX, mouseY);
    if (row == null || row.item() == null) {
      return;
    }
    List<Component> lines = new ArrayList<>();
    lines.add(row.label());
    lines.add(Component.literal(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(row.item()).toString())
        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    if (isSupplier() && row.state() == STATE_ALLOW) {
      lines.add(Component.translatable(key("tree.count_tip"))
          .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
  }

  @Nullable
  private Row rowAt(int left, int top, double mouseX, double mouseY) {
    int x0 = left + X;
    int y0 = top + Y;
    if (mouseX < x0 || mouseX >= x0 + W || mouseY < y0 || mouseY >= y0 + ROWS * ROW_H) {
      return null;
    }
    int index = scroll + (int) ((mouseY - y0) / ROW_H);
    return index < rows.size() ? rows.get(index) : null;
  }

  public void setEditingNode(@Nullable String node) {
    editingNode = node;
  }

  @Nullable
  public CountField countFieldAt(int left, int top, double mouseX, double mouseY) {
    if (!isSupplier()) {
      return null;
    }
    Row row = rowAt(left, top, mouseX, mouseY);
    if (row == null || !row.leaf() || row.state() != STATE_ALLOW) {
      return null;
    }
    int fieldX = left + X + W - 22 - COUNT_W;
    if (mouseX < fieldX || mouseX >= fieldX + COUNT_W
        || (hasScrollBar() && mouseX >= left + X + W - BAR_W)) {
      return null;
    }
    int index = rows.indexOf(row) - scroll;
    return new CountField(row.node(), fieldX + 2, top + Y + index * ROW_H + 3, COUNT_W - 5, row.count());
  }

  public boolean mouseClicked(int left, int top, double mouseX, double mouseY, int button) {
    int x0 = left + X;

    if (hasScrollBar() && mouseX >= x0 + W - BAR_W) {
      if (button == 0) {
        armedNode = null;
        draggingBar = true;
        int thumbTop = thumbY(top);
        dragGrab = mouseY >= thumbTop && mouseY < thumbTop + thumbHeight()
            ? mouseY - thumbTop
            : thumbHeight() / 2.0;
        dragTo(top, mouseY);
      }
      return true;
    }
    Row row = rowAt(left, top, mouseX, mouseY);
    if (row == null || button != 0) {
      return row != null;
    }
    if (!row.leaf() && query.trim().isEmpty() && mouseX < rowIndent(row, x0) + 9) {
      if (!expanded.remove(row.node())) {
        expanded.add(row.node());
      }
      rebuild();
      return true;
    }

    boolean onMark = mouseX >= x0 + W - 20 && mouseX < x0 + W - 7;
    boolean onIcon = row.item() != null
        && mouseX >= iconX(row, x0) && mouseX < iconX(row, x0) + 12;
    if (!onMark && !onIcon) {
      long now = net.minecraft.Util.getMillis();
      if (!row.node().equals(armedNode) || now - armedAt > DOUBLE_CLICK_MS) {
        armedNode = row.node();
        armedAt = now;
        return true;
      }
    }
    armedNode = null;

    ModNetworking.sendToServer(new PacketModuleTree(row.node(), row.state() == STATE_DENY));
    return true;
  }

  private void dragTo(int top, double mouseY) {
    int span = trackHeight() - thumbHeight();
    int maxScroll = Math.max(0, rows.size() - ROWS);
    if (span <= 0 || maxScroll <= 0) {
      scroll = 0;
      return;
    }
    double offset = mouseY - dragGrab - (top + Y + 1);
    scroll = (int) Math.round(Math.max(0, Math.min(span, offset)) * maxScroll / span);
  }

  public boolean mouseDragged(int top, double mouseY) {
    if (!draggingBar) {
      return false;
    }
    dragTo(top, mouseY);
    return true;
  }

  public void mouseReleased() {
    draggingBar = false;
  }

  public boolean mouseScrolled(int left, int top, double mouseX, double mouseY, double delta) {
    int x0 = left + X;
    int y0 = top + Y;
    if (mouseX < x0 || mouseX >= x0 + W || mouseY < y0 || mouseY >= y0 + ROWS * ROW_H) {
      return false;
    }
    int maxScroll = Math.max(0, rows.size() - ROWS);
    scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(delta)));
    armedNode = null;
    return true;
  }
}
