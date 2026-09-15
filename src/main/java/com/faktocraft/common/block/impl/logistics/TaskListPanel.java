package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import com.faktocraft.common.network.packet.PacketTableState;

public class TaskListPanel {

  public interface Host {
    void sendHistoryOp(int mode, long id);

    void requestRefresh();
  }

  static final int WRAP_X = 8;
  static final int WRAP_Y = 32;
  static final int WRAP_RIGHT = 332;
  static final int WRAP_BOTTOM = 248;
  static final int CARD_X = 12;
  static final int CARD_RIGHT = 320;
  static final int CARD_MIN_H = 20;
  static final int CARD_GAP = 2;
  static final int CARD_LINE_H = 9;
  static final int SUB_INDENT = 12;
  static final int TASKBAR_Y = 36;
  static final int TASKBAR_H = 14;
  static final int CARDS_TOP = 54;

  private enum StatusFilter {
    ALL, ACTIVE, DONE, FAILED
  }

  private enum TypeFilter {
    ALL, USER, SYSTEM
  }

  private enum CardType {
    PENDING(0xFFE6D492, 0xFFA89550),
    DONE(0xFFB4D8A4, 0xFF6E9C5C),
    FAILED(0xFFE2A4A4, 0xFFAE5A5A);

    final int fill;
    final int border;

    CardType(int fill, int border) {
      this.fill = fill;
      this.border = border;
    }
  }

  private record TaskCard(long id, ItemStack icon, Component line1, Component line2, CardType type,
      String stateKey, boolean system, List<PacketTableState.SubLine> subs) {

    boolean deletable() {
      return id != 0 && ("done".equals(stateKey) || stateKey.startsWith("error"));
    }

    boolean cancellable() {
      return id != 0 && !system && !stateKey.isEmpty() && !deletable();
    }

    boolean hasCross() {
      return deletable() || cancellable();
    }

    boolean expandable() {
      return id != 0 && !subs.isEmpty();
    }
  }

  private record TaskRow(TaskCard card, @Nullable PacketTableState.SubLine sub) {
  }

  private record RowText(List<FormattedCharSequence> lines1, List<FormattedCharSequence> lines2, int height,
      int textX, int iconX) {
  }

  private record RowHit(TaskRow row, int y0, int height) {
  }

  private final Minecraft minecraft;
  private final Font font;
  private final Host host;
  private final boolean showOrigin;

  private List<PacketTableState.TaskLine> tasks = List.of();
  private List<String> errors = List.of();
  private String query = "";
  private StatusFilter statusFilter = StatusFilter.ALL;
  private TypeFilter typeFilter = TypeFilter.ALL;
  private final Set<Long> expanded = new HashSet<>();
  private int scrollOffset;

  private EditBox search;
  private Button statusButton;
  @Nullable
  private Button typeButton;
  private Button clearButton;

  private List<TaskRow> rowsCache = List.of();
  private final List<RowText> measureCache = new ArrayList<>();
  private boolean dirty = true;

  public TaskListPanel(Minecraft minecraft, Font font, boolean showOrigin, Host host) {
    this.minecraft = minecraft;
    this.font = font;
    this.showOrigin = showOrigin;
    this.host = host;
  }

  private static String key(String name) {
    return "logistics." + Faktocraft.MODID + "." + name;
  }

  public void initWidgets(int left, int top, Consumer<AbstractWidget> adder) {
    int searchRight = showOrigin ? 104 : 140;
    search = new EditBox(font, left + 16, top + TASKBAR_Y + 3, searchRight - 20, 10, Component.empty());
    search.setBordered(false);
    search.setMaxLength(48);
    search.setTextColor(0xFFFFFF);
    search.setValue(query);
    search.setHint(Component.translatable(key("task_search_hint")).withStyle(ChatFormatting.DARK_GRAY));
    search.setResponder(text -> {
      query = text;
      scrollOffset = 0;
      dirty = true;
    });
    adder.accept(search);

    if (showOrigin) {
      typeButton = Button.builder(typeLabel(), b -> {
        typeFilter = TypeFilter.values()[(typeFilter.ordinal() + 1) % TypeFilter.values().length];
        typeButton.setMessage(typeLabel());
        scrollOffset = 0;
        dirty = true;
      }).bounds(left + 108, top + TASKBAR_Y, 66, TASKBAR_H).build();
      adder.accept(typeButton);
    }
    int statusX = showOrigin ? 178 : 144;
    int statusW = showOrigin ? 68 : 84;
    statusButton = Button.builder(statusLabel(), b -> {
      statusFilter = StatusFilter.values()[(statusFilter.ordinal() + 1) % StatusFilter.values().length];
      statusButton.setMessage(statusLabel());
      scrollOffset = 0;
      dirty = true;
    }).bounds(left + statusX, top + TASKBAR_Y, statusW, TASKBAR_H).build();
    adder.accept(statusButton);

    int clearX = showOrigin ? 250 : 232;
    int clearW = showOrigin ? 70 : 88;
    clearButton = Button.builder(Component.translatable(key("task_clear")), b -> {
      host.sendHistoryOp(com.faktocraft.common.network.packet.PacketTaskHistoryOp.MODE_CLEAR_DONE, 0);
      host.requestRefresh();
    }).bounds(left + clearX, top + TASKBAR_Y, clearW, TASKBAR_H)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.translatable(key("task_clear_tooltip"))))
        .build();
    adder.accept(clearButton);
  }

  private Component statusLabel() {
    return Component.translatable(key("task_filter." + statusFilter.name().toLowerCase(Locale.ROOT)));
  }

  private Component typeLabel() {
    return Component.translatable(key("task_type." + typeFilter.name().toLowerCase(Locale.ROOT)));
  }

  public void setVisible(boolean visible) {
    search.setVisible(visible);
    statusButton.visible = visible;
    clearButton.visible = visible;
    if (typeButton != null) {
      typeButton.visible = visible;
    }
  }

  public EditBox searchBox() {
    return search;
  }

  public void setData(List<PacketTableState.TaskLine> tasks, List<String> errors) {
    this.tasks = tasks;
    this.errors = errors;
    dirty = true;
  }

  private CardType typeOf(String stateKey) {
    if ("done".equals(stateKey)) {
      return CardType.DONE;
    }
    if (stateKey.startsWith("error") || "cancelled".equals(stateKey) || "failed".equals(stateKey)) {
      return CardType.FAILED;
    }
    return CardType.PENDING;
  }

  private boolean taskVisible(PacketTableState.TaskLine task) {
    CardType type = typeOf(task.stateKey());
    boolean statusOk = switch (statusFilter) {
      case ALL -> true;
      case ACTIVE -> type == CardType.PENDING;
      case DONE -> type == CardType.DONE;
      case FAILED -> type == CardType.FAILED;
    };
    boolean typeOk = switch (typeFilter) {
      case ALL -> true;
      case USER -> !task.system();
      case SYSTEM -> task.system();
    };
    if (!statusOk || !typeOk) {
      return false;
    }
    String needle = query.toLowerCase(Locale.ROOT).trim();
    return needle.isEmpty()
        || task.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle);
  }

  private Component originText(PacketTableState.TaskLine task) {
    BlockPos pos = BlockPos.of(task.originPos());
    String where = (task.originLabel().isEmpty() ? "" : task.originLabel() + " ")
        + "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    return Component.translatable(key("origin"), where);
  }

  private List<TaskCard> buildCards() {
    List<TaskCard> cards = new ArrayList<>();

    for (String error : errors) {
      String[] parts = error.split(":", 2);
      Component text = parts.length > 1
          ? Component.translatable(key("error." + parts[0]), parts[1])
          : Component.translatable(key("error." + parts[0]));
      cards.add(new TaskCard(0, ItemStack.EMPTY, text, Component.empty(), CardType.FAILED, "", true, List.of()));
    }
    for (PacketTableState.TaskLine task : tasks) {
      if (!taskVisible(task)) {
        continue;
      }
      Component line1 = Component.literal(task.count() + "× ").append(task.stack().getHoverName());
      MutableComponent line2 = Component.translatable(key("state." + task.stateKey()), task.detail());
      if (task.count() > 0 && !"pending".equals(task.stateKey())) {
        line2 = line2.append(" · ").append(Component.translatable(key("progress"), task.delivered(),
            task.count()));
      }
      if (showOrigin && task.originPos() != 0) {
        line2 = line2.append(" · ").append(originText(task));
      }
      cards.add(new TaskCard(task.id(), task.stack(), line1, line2, typeOf(task.stateKey()),
          task.stateKey(), task.system(), task.subs()));
    }
    return cards;
  }

  private List<TaskRow> buildRows() {
    List<TaskRow> rows = new ArrayList<>();
    for (TaskCard card : buildCards()) {
      rows.add(new TaskRow(card, null));
      if (card.expandable() && expanded.contains(card.id())) {
        for (PacketTableState.SubLine sub : card.subs()) {
          rows.add(new TaskRow(card, sub));
        }
      }
    }
    return rows;
  }

  private Component subLine1(PacketTableState.SubLine sub) {
    return Component.translatable(key("sub." + sub.kind()), sub.count(), sub.stack().getHoverName());
  }

  private Component subLine2(PacketTableState.SubLine sub) {
    MutableComponent text = Component.translatable(key("state." + sub.stateKey()), "");
    if (sub.count() > 0) {
      text.append(" · ").append(Component.translatable(key("sub_progress"), sub.done(), sub.count()));
    }
    if (sub.inputsTotal() > 0) {
      text.append(" · ").append(Component.translatable(key("sub_inputs"), sub.inputsDone(), sub.inputsTotal()));
    }
    if (sub.leftoverCount() > 0) {
      text.append(" · ").append(Component.translatable(key("leftover"), sub.leftoverCount(),
          sub.leftoverStack().getHoverName()));
      if (!sub.where().isEmpty()) {
        text.append(" ").append(Component.translatable(key("leftover." + sub.where()), sub.whereDetail()));
      }
    }
    return text;
  }

  private RowText measure(TaskRow row) {
    boolean isSub = row.sub() != null;
    int x0 = CARD_X + (isSub ? SUB_INDENT : 0);
    ItemStack icon = isSub ? row.sub().stack() : row.card().icon();
    int textX = x0 + (icon.isEmpty() ? 4 : 22);
    int reserved = 0;
    if (!isSub) {
      if (row.card().deletable()) {
        reserved += 12;
      }
      if (row.card().expandable()) {
        reserved += 12;
      }
    }
    int textWidth = Math.max(24, CARD_RIGHT - textX - 4 - reserved);
    Component first = isSub ? subLine1(row.sub()) : row.card().line1();
    Component second = isSub ? subLine2(row.sub()) : row.card().line2();
    List<FormattedCharSequence> lines1 = font.split(first, textWidth);
    List<FormattedCharSequence> lines2 = second.getString().isEmpty()
        ? List.of()
        : font.split(second, textWidth);
    int height = Math.max(CARD_MIN_H, 4 + (lines1.size() + lines2.size()) * CARD_LINE_H);
    return new RowText(lines1, lines2, height, textX, x0 + 2);
  }

  private int cardsBottom() {
    return WRAP_BOTTOM - 4;
  }

  private void refreshRowCache() {
    rowsCache = buildRows();
    measureCache.clear();
    for (TaskRow row : rowsCache) {
      measureCache.add(measure(row));
    }
  }

  private int maxScroll() {
    int available = cardsBottom() - CARDS_TOP;
    int used = 0;
    for (int i = measureCache.size() - 1; i >= 0; i--) {
      used += measureCache.get(i).height() + CARD_GAP;
      if (used > available) {
        return i + 1;
      }
    }
    return 0;
  }

  public void render(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {

    graphics.fill(left + WRAP_X, top + WRAP_Y, left + WRAP_RIGHT, top + WRAP_BOTTOM, 0xFF8B8B8B);
    graphics.fill(left + WRAP_X, top + WRAP_Y, left + WRAP_RIGHT, top + WRAP_Y + 1, 0xFF373737);
    graphics.fill(left + WRAP_X, top + WRAP_Y, left + WRAP_X + 1, top + WRAP_BOTTOM, 0xFF373737);
    graphics.fill(left + WRAP_X, top + WRAP_BOTTOM - 1, left + WRAP_RIGHT, top + WRAP_BOTTOM, 0xFFFFFFFF);
    graphics.fill(left + WRAP_RIGHT - 1, top + WRAP_Y, left + WRAP_RIGHT, top + WRAP_BOTTOM, 0xFFFFFFFF);

    int searchRight = showOrigin ? 104 : 140;
    graphics.fill(left + CARD_X, top + TASKBAR_Y, left + searchRight, top + TASKBAR_Y + TASKBAR_H, 0xFF000000);
    graphics.fill(left + CARD_X + 1, top + TASKBAR_Y + 1, left + searchRight - 1,
        top + TASKBAR_Y + TASKBAR_H - 1, 0xFF1E1E1E);

    if (dirty) {
      refreshRowCache();
      dirty = false;
    }
    int maxScroll = maxScroll();
    scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

    if (rowsCache.isEmpty()) {
      graphics.drawString(font, Component.translatable(key("no_tasks")),
          left + CARD_X + 2, top + CARDS_TOP + 4, 0x555555, false);
    }
    int totalHeight = 0;
    for (RowText text : measureCache) {
      totalHeight += text.height() + CARD_GAP;
    }
    int y = top + CARDS_TOP;
    int bottom = top + cardsBottom();
    for (int i = scrollOffset; i < rowsCache.size(); i++) {
      RowText text = measureCache.get(i);
      if (y + text.height() > bottom) {
        break;
      }
      renderRow(graphics, rowsCache.get(i), text, left, y, mouseX, mouseY);
      y += text.height() + CARD_GAP;
    }

    int available = cardsBottom() - CARDS_TOP;
    if (totalHeight > available && maxScroll > 0) {
      int trackTop = top + CARDS_TOP;
      int trackBottom = top + cardsBottom();
      int trackHeight = trackBottom - trackTop;
      int thumbHeight = Math.max(12, trackHeight * available / totalHeight);
      int thumbTop = trackTop + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
      graphics.fill(left + 324, trackTop, left + 328, trackBottom, 0xFF6E6E6E);
      graphics.fill(left + 324, thumbTop, left + 328, thumbTop + thumbHeight, 0xFF3A3A3A);
    }
  }

  private void renderRow(GuiGraphics graphics, TaskRow row, RowText text, int left, int y0, int mouseX,
      int mouseY) {
    boolean isSub = row.sub() != null;
    CardType type = isSub ? typeOf(row.sub().stateKey()) : row.card().type();
    int x0 = left + CARD_X + (isSub ? SUB_INDENT : 0);
    int x1 = left + CARD_RIGHT;
    int y1 = y0 + text.height();
    graphics.fill(x0, y0, x1, y1, type.fill);
    graphics.fill(x0, y0, x1, y0 + 1, type.border);
    graphics.fill(x0, y1 - 1, x1, y1, type.border);
    graphics.fill(x0, y0, x0 + 1, y1, type.border);
    graphics.fill(x1 - 1, y0, x1, y1, type.border);

    ItemStack icon = isSub ? row.sub().stack() : row.card().icon();
    if (!icon.isEmpty()) {
      graphics.renderItem(icon, left + text.iconX(), y0 + (text.height() - 16) / 2);
    }
    if (!isSub) {
      if (row.card().hasCross()) {
        drawDeleteCross(graphics, x1 - 11, y0 + (text.height() - 6) / 2,
            overDelete(x1, y0, text.height(), mouseX, mouseY));
      }
      if (row.card().expandable()) {
        drawChevron(graphics, x1 - (row.card().hasCross() ? 24 : 12), y0 + (text.height() - 7) / 2,
            expanded.contains(row.card().id()), 0xFF4A4A4A);
      }
    }
    int lineCount = text.lines1().size() + text.lines2().size();
    int textY = y0 + (text.height() - lineCount * CARD_LINE_H) / 2 + 1;
    for (FormattedCharSequence line : text.lines1()) {
      graphics.drawString(font, line, left + text.textX(), textY, 0x2E2E2E, false);
      textY += CARD_LINE_H;
    }
    for (FormattedCharSequence line : text.lines2()) {
      graphics.drawString(font, line, left + text.textX(), textY, 0x4A4A4A, false);
      textY += CARD_LINE_H;
    }
  }

  private void drawChevron(GuiGraphics graphics, int x, int y, boolean open, int color) {
    if (open) {
      for (int row = 0; row < 4; row++) {
        graphics.fill(x + row, y + row, x + 7 - row, y + row + 1, color);
      }
    } else {
      for (int col = 0; col < 4; col++) {
        graphics.fill(x + col, y + col, x + col + 1, y + 7 - col, color);
      }
    }
  }

  private void drawDeleteCross(GuiGraphics graphics, int x, int y, boolean hovered) {
    int color = hovered ? 0xFFC03030 : 0xFF6A6A6A;
    for (int i = 0; i < 6; i++) {
      graphics.fill(x + i, y + i, x + i + 1, y + i + 1, color);
      graphics.fill(x + 5 - i, y + i, x + 6 - i, y + i + 1, color);
    }
  }

  private boolean overDelete(int cardX1, int cardY0, int cardHeight, double mouseX, double mouseY) {
    return mouseX >= cardX1 - 14 && mouseX < cardX1 - 2
        && mouseY >= cardY0 && mouseY < cardY0 + cardHeight;
  }

  @Nullable
  private RowHit rowAt(int left, int top, double mouseX, double mouseY) {
    if (mouseX < left + CARD_X || mouseX >= left + CARD_RIGHT
        || measureCache.size() != rowsCache.size()) {
      return null;
    }
    int y = top + CARDS_TOP;
    int bottom = top + cardsBottom();
    for (int i = scrollOffset; i < rowsCache.size(); i++) {
      int height = measureCache.get(i).height();
      if (y + height > bottom) {
        break;
      }
      if (mouseY >= y && mouseY < y + height) {
        return new RowHit(rowsCache.get(i), y, height);
      }
      y += height + CARD_GAP;
    }
    return null;
  }

  public void renderTooltip(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
    RowHit hit = rowAt(left, top, mouseX, mouseY);
    if (hit == null || hit.row().sub() != null || !hit.row().card().hasCross()) {
      return;
    }
    if (overDelete(left + CARD_RIGHT, hit.y0(), hit.height(), mouseX, mouseY)) {
      graphics.renderTooltip(font, Component.translatable(
          key(hit.row().card().deletable() ? "task_delete" : "task_cancel")), mouseX, mouseY);
    }
  }

  public boolean mouseClicked(int left, int top, double mouseX, double mouseY) {
    RowHit hit = rowAt(left, top, mouseX, mouseY);
    if (hit == null || hit.row().sub() != null) {
      return false;
    }
    TaskCard card = hit.row().card();
    if (card.hasCross() && overDelete(left + CARD_RIGHT, hit.y0(), hit.height(), mouseX, mouseY)) {
      host.sendHistoryOp(card.deletable()
          ? com.faktocraft.common.network.packet.PacketTaskHistoryOp.MODE_DELETE
          : com.faktocraft.common.network.packet.PacketTaskHistoryOp.MODE_CANCEL, card.id());
      host.requestRefresh();
      click();
      return true;
    }
    if (card.expandable()) {
      if (!expanded.remove(card.id())) {
        expanded.add(card.id());
      }
      dirty = true;
      click();
      return true;
    }
    return false;
  }

  public void mouseScrolled(double delta) {
    scrollOffset = Math.max(0, scrollOffset + (delta < 0 ? 1 : -1));
  }

  private void click() {
    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
  }
}
