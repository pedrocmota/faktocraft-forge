package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketMenuAction;
import com.faktocraft.common.util.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class ScreenPipeRecipes<M extends MenuPipeRecipes> extends AbstractContainerScreen<M> {

  protected static final int SLOT_U = 84;
  protected static final int SLOT_V = 27;

  private static final int TOP_BAND = 120;
  private static final int BOTTOM_BAND = 100;
  private static final int STRIP_V = 40;
  private static final int STRIP_H = 80;

  protected static final int PAD = MenuPipeRecipes.PAD;
  protected static final int LIST_X = 8;
  protected static final int LIST_W = MenuPipeRecipes.WIDTH - 2 * LIST_X;
  protected static final int SEARCH_Y = 18;
  protected static final int LIST_Y = 34;
  protected static final int ROW_H = 20;

  private static final int CARD_X = LIST_X + 2;
  private static final int CARD_W = LIST_W - 11;
  private static final int BAR_X = LIST_X + LIST_W - 6;

  private static final int FRAME_FILL = 0xFF8B8B8B;
  private static final int FRAME_DARK = 0xFF373737;
  private static final int FRAME_LIGHT = 0xFFFFFFFF;
  private static final int CARD_FILL = 0xFFC6C6C6;
  private static final int CARD_BORDER = 0xFF6A6A6A;
  private static final int CARD_HOVER_FILL = 0xFFDCDCDC;
  private static final int CARD_HOVER_BORDER = 0xFF4A4A4A;
  private static final int CARD_HELD_FILL = 0xFFA8A8A8;
  private static final int SEARCH_BORDER = 0xFF000000;
  private static final int SEARCH_FILL = 0xFF1E1E1E;
  private static final int BAR_TRACK = 0xFF6E6E6E;
  private static final int BAR_THUMB = 0xFF3A3A3A;
  private static final int TEXT = 0x2E2E2E;
  private static final int TEXT_DIM = 0x555555;
  private static final int DROP_MARK = 0xFFE8B923;

  private static final int DRAG_SLOP = 3;

  private int listScroll;

  private int filteredCount = -1;
  private String filteredQuery;
  private int filterCooldown;
  private int builtEdit = Integer.MIN_VALUE;
  private Button addButton;
  private EditBox search;
  private String query = "";

  private final List<Integer> visible = new ArrayList<>();

  private boolean draggingBar;
  private double barGrab;

  private int edgeScrollCooldown;
  private int grabbedRow = -1;
  private double grabbedAtY;
  private boolean dragging;
  private double dragY;

  protected ScreenPipeRecipes(M menu, Inventory inventory, Component title, int width, int height) {
    super(menu, inventory, title);
    this.imageWidth = width;
    this.imageHeight = height;

    this.inventoryLabelX = MenuPipeRecipes.PLAYER_INV_X - 1;
  }

  protected String key(String name) {
    return "logistics." + Faktocraft.MODID + "." + name;
  }

  protected abstract int listRows();

  protected abstract int maxEntries();

  protected abstract ItemStack entryIcon(int index);

  protected abstract Component entryLabel(int index);

  protected abstract Component listWarning();

  protected abstract int addButtonY();

  protected abstract int removeButtonX();

  protected abstract void buildDetailWidgets(int left, int top);

  protected abstract void renderDetailBg(GuiGraphics graphics);

  protected abstract void renderDetailLabels(GuiGraphics graphics);

  protected abstract void renderDetailHover(GuiGraphics graphics, int mouseX, int mouseY);

  protected abstract boolean detailMouseClicked(double mouseX, double mouseY, int button);

  protected abstract boolean detailMouseScrolled(double mouseX, double mouseY, double delta);

  public boolean isEditing() {
    return this.menu.editIndex() >= 0;
  }

  protected void press(int id) {
    ModNetworking.sendToServer(new PacketMenuAction(this.menu.containerId, id));
  }

  @Override
  protected void init() {
    if (this.minecraft != null) {

      com.faktocraft.client.GuiScaleHelper.fit(this.minecraft, this, this.imageWidth + 8, this.imageHeight, 16);
    }
    super.init();
    builtEdit = Integer.MIN_VALUE;
    buildView();
  }

  @Override
  public void removed() {
    if (this.minecraft != null) {
      com.faktocraft.client.GuiScaleHelper.restore(this.minecraft);
    }
    super.removed();
  }

  private void buildView() {
    clearWidgets();
    builtEdit = this.menu.editIndex();
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    if (builtEdit < 0) {
      addButton = addRenderableWidget(Button.builder(Component.translatable(key("recipes.add")),
          b -> press(MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_ADD, 0)))
          .bounds(left + LIST_X, top + addButtonY(), LIST_W, 16).build());
      search = new EditBox(this.font, left + LIST_X + 4, top + SEARCH_Y + 3, LIST_W - 8, 10,
          Component.empty());
      search.setBordered(false);
      search.setMaxLength(48);
      search.setTextColor(0xFFFFFF);
      search.setValue(query);
      search.setHint(Component.translatable(key("search_hint")).withStyle(ChatFormatting.GRAY));
      search.setResponder(text -> {
        query = text;
        listScroll = 0;
        refreshFilter();
      });
      addRenderableWidget(search);

      addRenderableWidget(new com.faktocraft.common.screen.button.GuiCopyPasteButton(
          left + MenuPipeRecipes.WIDTH - 34, top + 4, false,
          b -> press(MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_COPY_CONFIG, 0)),
          Component.translatable("gui." + com.faktocraft.Faktocraft.MODID + ".config.copy")));
      addRenderableWidget(new com.faktocraft.common.screen.button.GuiCopyPasteButton(
          left + MenuPipeRecipes.WIDTH - 20, top + 4, true,
          b -> press(MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_PASTE_CONFIG, 0)),
          Component.translatable("gui." + com.faktocraft.Faktocraft.MODID + ".config.paste")));
      refreshFilter();
    } else {
      addButton = null;
      search = null;
      cancelDrag();
      addRenderableWidget(Button.builder(Component.translatable(key("recipes.back")),
          b -> press(MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_BACK, 0)))
          .bounds(left + PAD + 8, top + 18, 44, 14).build());
      addRenderableWidget(Button.builder(Component.translatable(key("recipes.remove")),
          b -> press(MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_REMOVE, 0)))
          .bounds(left + removeButtonX(), top + 18, 48, 14).build());
      buildDetailWidgets(left, top);
    }
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (this.menu.editIndex() != builtEdit) {
      buildView();
    }
    if (addButton != null) {
      addButton.active = this.menu.entryCount() < maxEntries();
    }
    if (!isEditing() && (this.menu.entryCount() != filteredCount || !query.equals(filteredQuery)
        || --filterCooldown <= 0)) {
      refreshFilter();
    }
    tickEdgeScroll();

    if (grabbedRow >= visible.size()) {
      cancelDrag();
    }
    int maxScroll = Math.max(0, visible.size() - visibleRows());
    if (listScroll > maxScroll) {
      listScroll = maxScroll;
    }
  }

  private boolean hasScrollBar() {
    return visible.size() > visibleRows();
  }

  private int trackHeight() {
    return visibleRows() * ROW_H - 2;
  }

  private int thumbHeight() {
    return Math.max(8, trackHeight() * visibleRows() / Math.max(1, visible.size()));
  }

  private int thumbY() {
    int maxScroll = Math.max(1, visible.size() - visibleRows());
    return LIST_Y + (trackHeight() - thumbHeight()) * listScroll / maxScroll;
  }

  private int visibleRows() {
    return Math.max(1, listRows() - (listWarning().getString().isEmpty() ? 0 : 1));
  }

  private void refreshFilter() {
    filteredCount = this.menu.entryCount();
    filteredQuery = query;
    filterCooldown = 20;
    visible.clear();
    String needle = query.trim().toLowerCase(Locale.ROOT);
    for (int i = 0; i < this.menu.entryCount(); i++) {
      if (needle.isEmpty() || entryLabel(i).getString().toLowerCase(Locale.ROOT).contains(needle)) {
        visible.add(i);
      }
    }
  }

  private void cancelDrag() {
    grabbedRow = -1;
    dragging = false;
  }

  private void dragBarTo(double mouseY) {
    int span = trackHeight() - thumbHeight();
    int maxScroll = Math.max(0, visible.size() - visibleRows());
    if (span <= 0 || maxScroll <= 0) {
      listScroll = 0;
      return;
    }
    double offset = mouseY - barGrab - (this.topPos + LIST_Y);
    listScroll = (int) Math.round(Math.max(0, Math.min(span, offset)) * maxScroll / span);
  }

  private void tickEdgeScroll() {
    if (!dragging) {
      edgeScrollCooldown = 0;
      return;
    }
    double top = this.topPos + LIST_Y;
    double bottom = top + visibleRows() * ROW_H;
    int direction = dragY < top + ROW_H ? -1 : dragY > bottom - ROW_H ? 1 : 0;
    if (direction == 0) {
      edgeScrollCooldown = 0;
      return;
    }
    if (--edgeScrollCooldown > 0) {
      return;
    }
    edgeScrollCooldown = 3;
    int maxScroll = Math.max(0, visible.size() - visibleRows());
    listScroll = Math.max(0, Math.min(maxScroll, listScroll + direction));
  }

  private int rowAt(double mouseX, double mouseY) {
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    double relX = mouseX - left - LIST_X;
    double relY = mouseY - top - LIST_Y;
    if (relX < 0 || relX >= LIST_W || relY < 0 || relY >= visibleRows() * ROW_H
        || (hasScrollBar() && relX >= BAR_X - LIST_X)) {
      return -1;
    }
    int row = listScroll + (int) (relY / ROW_H);
    return row < visible.size() ? row : -1;
  }

  private int dropRow(double mouseY) {
    int top = (this.height - this.imageHeight) / 2;
    double relY = mouseY - top - LIST_Y;
    int row = listScroll + (int) Math.round(relY / ROW_H);
    return Math.max(0, Math.min(visible.size(), row));
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (isEditing()) {
      if (detailMouseClicked(mouseX, mouseY, button)) {
        return true;
      }
      return super.mouseClicked(mouseX, mouseY, button);
    }
    int relX = (int) (mouseX - this.leftPos);
    if (hasScrollBar() && button == 0 && relX >= BAR_X && relX < BAR_X + 5
        && mouseY >= this.topPos + LIST_Y && mouseY < this.topPos + LIST_Y + trackHeight()) {
      cancelDrag();
      draggingBar = true;
      double thumbTop = this.topPos + thumbY();
      barGrab = mouseY >= thumbTop && mouseY < thumbTop + thumbHeight()
          ? mouseY - thumbTop : thumbHeight() / 2.0;
      dragBarTo(mouseY);
      return true;
    }
    int row = rowAt(mouseX, mouseY);
    if (row >= 0 && button == 0) {

      grabbedRow = row;
      grabbedAtY = mouseY;
      dragY = mouseY;
      dragging = false;
      setFocused(null);
      if (search != null) {
        search.setFocused(false);
      }
      return true;
    }
    cancelDrag();
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY0) {
    if (draggingBar) {
      dragBarTo(mouseY);
      return true;
    }
    if (grabbedRow >= 0) {
      dragY = mouseY;
      if (!dragging && Math.abs(mouseY - grabbedAtY) > DRAG_SLOP) {
        dragging = true;
      }
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY0);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (draggingBar && button == 0) {
      draggingBar = false;
      return true;
    }
    if (grabbedRow >= 0 && button == 0) {
      int row = grabbedRow;
      boolean moved = dragging;
      cancelDrag();
      if (row >= visible.size()) {
        return true;
      }
      if (!moved) {
        press(MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_OPEN, visible.get(row)));
        return true;
      }

      int gap = dropRow(mouseY);
      int from = visible.get(row);
      int to = gap >= visible.size() ? this.menu.entryCount() : visible.get(gap);
      if (from != to && !(gap == row) && !(gap == row + 1)) {
        press(MenuPipeRecipes.encodeMove(from, to));
      }
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (isEditing()) {
      if (detailMouseScrolled(mouseX, mouseY, delta)) {
        return true;
      }
      return super.mouseScrolled(mouseX, mouseY, delta);
    }
    int maxScroll = Math.max(0, visible.size() - visibleRows());
    if (maxScroll > 0) {
      listScroll = Math.max(0, Math.min(maxScroll, listScroll - (int) Math.signum(delta)));
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (search != null && search.isFocused()) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
        search.setFocused(false);
        return true;
      }
      if (search.keyPressed(keyCode, scanCode, modifiers)) {
        return true;
      }

      return keyCode != GLFW.GLFW_KEY_TAB;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);
    super.render(graphics, mouseX, mouseY, partialTick);
    renderTooltip(graphics, mouseX, mouseY);
    if (isEditing()) {
      renderDetailHover(graphics, mouseX, mouseY);
    }
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

    int band = (this.imageWidth + 1) / 2;
    int rightU = 176 - band;
    int rightX = this.leftPos + this.imageWidth - band;
    int texH = textureHeight();
    blitBand(graphics, band, rightU, rightX, this.topPos, 0, TOP_BAND);
    int bottomY = this.topPos + this.imageHeight - BOTTOM_BAND;
    blitBand(graphics, band, rightU, rightX, bottomY, texH - BOTTOM_BAND, BOTTOM_BAND);
    for (int y = this.topPos + TOP_BAND; y < bottomY; y += STRIP_H) {
      blitBand(graphics, band, rightU, rightX, y, STRIP_V, Math.min(STRIP_H, bottomY - y));
    }
    renderPlayerInventory(graphics);
    if (isEditing()) {
      renderDetailBg(graphics);
      return;
    }

    int left = this.leftPos + LIST_X;
    int top = this.topPos + SEARCH_Y;
    graphics.fill(left, top, left + LIST_W, top + 14, SEARCH_BORDER);
    graphics.fill(left + 1, top + 1, left + LIST_W - 1, top + 13, SEARCH_FILL);
  }

  private void renderPlayerInventory(GuiGraphics graphics) {
    int invY = this.menu.playerInvY();
    graphics.fill(this.leftPos + 3, this.topPos + invY - 1, this.leftPos + this.imageWidth - 3,
        this.topPos + invY + 76, 0xFFC6C6C6);
    for (int i = 0; i < 27; i++) {
      slotFrame(graphics, MenuPipeRecipes.PLAYER_INV_X + (i % 9) * 18, invY + (i / 9) * 18);
    }
    for (int col = 0; col < 9; col++) {
      slotFrame(graphics, MenuPipeRecipes.PLAYER_INV_X + col * 18, invY + 58);
    }
  }

  private void blitBand(GuiGraphics graphics, int band, int rightU, int rightX, int y, int v, int height) {
    graphics.blit(getGuiTexture(), this.leftPos, y, 0, v, band, height, 256, 256);
    graphics.blit(getGuiTexture(), rightX, y, rightU, v, band, height, 256, 256);
  }

  protected abstract net.minecraft.resources.ResourceLocation getGuiTexture();

  protected abstract int textureHeight();

  protected void slotFrame(GuiGraphics graphics, int x, int y) {
    graphics.blit(Constants.PROCESS, this.leftPos + x - 1, this.topPos + y - 1, SLOT_U, SLOT_V, 18, 18, 256, 256);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
        0x404040, false);
    if (isEditing()) {
      renderDetailLabels(graphics);
      return;
    }
    renderList(graphics, mouseX - this.leftPos, mouseY - this.topPos);
  }

  private void renderList(GuiGraphics graphics, int relX, int relY) {
    int rows = visibleRows();
    int frameBottom = LIST_Y + rows * ROW_H;
    frame(graphics, LIST_X, LIST_Y - 2, LIST_W, frameBottom - LIST_Y + 3);

    Component warning = listWarning();
    if (!warning.getString().isEmpty()) {
      var line = Component.empty().append(warning).withStyle(ChatFormatting.DARK_RED).getVisualOrderText();
      graphics.drawString(this.font, line, LIST_X + (LIST_W - this.font.width(line)) / 2,
          frameBottom + 5, 0x404040, false);
    }
    if (visible.isEmpty()) {
      Component empty = Component.translatable(key(
          this.menu.entryCount() == 0 ? "recipes.empty" : "recipes.no_match"));
      graphics.drawString(this.font, empty, LIST_X + (LIST_W - this.font.width(empty)) / 2,
          LIST_Y + (rows * ROW_H - 8) / 2, TEXT_DIM, false);
      return;
    }
    for (int i = 0; i < rows; i++) {
      int row = listScroll + i;
      if (row >= visible.size()) {
        break;
      }
      if (dragging && row == grabbedRow) {
        continue;
      }
      int y = LIST_Y + i * ROW_H;

      boolean hovered = !dragging && relX >= LIST_X
          && relX < (hasScrollBar() ? BAR_X : LIST_X + LIST_W)
          && relY >= y && relY < y + ROW_H;
      drawRow(graphics, visible.get(row), CARD_X, y, hovered ? CARD_HOVER_FILL : CARD_FILL,
          hovered ? CARD_HOVER_BORDER : CARD_BORDER);
    }
    if (dragging) {
      renderDrag(graphics);
    }
    if (hasScrollBar()) {
      graphics.fill(BAR_X, LIST_Y, BAR_X + 4, LIST_Y + trackHeight(), BAR_TRACK);
      graphics.fill(BAR_X, thumbY(), BAR_X + 4, thumbY() + thumbHeight(),
          draggingBar ? FRAME_DARK : BAR_THUMB);
    }
  }

  private void frame(GuiGraphics graphics, int x, int y, int width, int height) {
    graphics.fill(x, y, x + width, y + height, FRAME_FILL);
    graphics.fill(x, y, x + width, y + 1, FRAME_DARK);
    graphics.fill(x, y, x + 1, y + height, FRAME_DARK);
    graphics.fill(x, y + height - 1, x + width, y + height, FRAME_LIGHT);
    graphics.fill(x + width - 1, y, x + width, y + height, FRAME_LIGHT);
  }

  private void drawRow(GuiGraphics graphics, int index, int x, int y, int fill, int border) {
    int bottom = y + ROW_H - 2;
    graphics.fill(x, y, x + CARD_W, bottom, fill);
    graphics.fill(x, y, x + CARD_W, y + 1, border);
    graphics.fill(x, bottom - 1, x + CARD_W, bottom, border);
    graphics.fill(x, y, x + 1, bottom, border);
    graphics.fill(x + CARD_W - 1, y, x + CARD_W, bottom, border);
    ItemStack icon = entryIcon(index);
    if (!icon.isEmpty()) {
      graphics.renderItem(icon, x + 2, y + 1);
    }
    graphics.drawString(this.font,
        net.minecraft.locale.Language.getInstance().getVisualOrder(
            this.font.substrByWidth(entryLabel(index), CARD_W - 26)),
        x + 22, y + 5, TEXT, false);
  }

  private void renderDrag(GuiGraphics graphics) {
    int rows = visibleRows();
    int gap = dropRow(dragY) - listScroll;
    int markY = LIST_Y + Math.max(0, Math.min(rows, gap)) * ROW_H - 1;
    graphics.fill(CARD_X, markY, CARD_X + CARD_W, markY + 2, DROP_MARK);
    int y = (int) Math.round(dragY - (this.height - this.imageHeight) / 2.0) - ROW_H / 2;
    y = Math.max(LIST_Y - ROW_H, Math.min(LIST_Y + rows * ROW_H, y));
    drawRow(graphics, visible.get(grabbedRow), CARD_X + 2, y, CARD_HELD_FILL, CARD_HOVER_BORDER);
  }
}
