package com.faktocraft.client;

import com.faktocraft.common.screen.widgets.FilteredEditBox;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.scan.ScanChannels;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class ScanCodePopup {

  public static final int ICON_SIZE = 10;

  private static final int FIELD_W = 70;
  private static final int BUTTON_W = 54;
  private static final int PAD = 6;
  private static final int LABEL_H = 10;
  private static final int FIELD_H = 14;
  private static final int PENDING_TICKS = 20;
  private static final int TOOLTIP_W = 180;

  private final Screen screen;
  private final Font font;
  private final Supplier<String> current;
  private final IntConsumer onApply;
  private final FilteredEditBox box;
  private final DeviceButton button;

  private boolean open;
  private int pending;
  private int iconX;
  private int iconY;

  public ScanCodePopup(Screen screen, Font font, Supplier<String> current, IntConsumer onApply) {
    this.screen = screen;
    this.font = font;
    this.current = current;
    this.onApply = onApply;
    box = new FilteredEditBox(font, 0, 0, FIELD_W, FIELD_H, Component.empty());
    box.setMaxLength(ScanChannels.CODE_DIGITS);
    box.setFilter(ScanCodePopup::digitsOnly);
    box.setHint(Component.translatable(key("hint")).withStyle(ChatFormatting.DARK_GRAY));
    box.setValue(current.get());
    box.visible = false;
    button = new DeviceButton(0, 0, BUTTON_W, FIELD_H + 2, Component.translatable(key("apply")), b -> apply());
    button.visible = false;
  }

  private static String key(String name) {
    return "gui." + Faktocraft.MODID + ".scan_code." + name;
  }

  private static boolean digitsOnly(String text) {
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) < '0' || text.charAt(i) > '9') {
        return false;
      }
    }
    return true;
  }

  public EditBox box() {
    return box;
  }

  public Button button() {
    return button;
  }

  public boolean isOpen() {
    return open;
  }

  public void setIcon(int x, int y) {
    iconX = x;
    iconY = y;
  }

  private boolean canApply() {
    return box.getValue().length() == ScanChannels.CODE_DIGITS;
  }

  private void apply() {
    if (!canApply()) {
      return;
    }
    onApply.accept(Integer.parseInt(box.getValue()));
    pending = PENDING_TICKS;
    unfocus();
  }

  private void unfocus() {
    box.setFocused(false);
    screen.setFocused(null);
  }

  public void close() {
    open = false;
    unfocus();
  }

  public void tick() {
    if (pending > 0) {
      pending--;
    }
    String value = current.get();
    if (pending == 0 && !box.isFocused() && !box.getValue().equals(value)) {
      box.setValue(value);
    }
    button.active = canApply() && !box.getValue().equals(value);
    box.visible = open;
    button.visible = open;
  }

  private boolean overIcon(double mouseX, double mouseY) {
    return mouseX >= iconX - 2 && mouseX < iconX + ICON_SIZE + 2 && mouseY >= iconY - 2
        && mouseY < iconY + ICON_SIZE + 2;
  }

  private int panelW() {
    return PAD + FIELD_W + 6 + BUTTON_W + PAD;
  }

  private int panelH() {
    return PAD + LABEL_H + FIELD_H + PAD;
  }

  private int panelLeft() {
    return iconX + ICON_SIZE - panelW();
  }

  private int panelTop() {
    return iconY + ICON_SIZE + 4;
  }

  private boolean overPanel(double mouseX, double mouseY) {
    return mouseX >= panelLeft() && mouseX < panelLeft() + panelW() && mouseY >= panelTop()
        && mouseY < panelTop() + panelH();
  }

  public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    boolean highlight = open || overIcon(mouseX, mouseY);
    int color = highlight ? 0xFFE8C43A : 0xFF8A8E94;
    int x = iconX;
    int y = iconY;
    graphics.fill(x + 3, y, x + 7, y + 1, color);
    graphics.fill(x + 2, y + 1, x + 3, y + 4, color);
    graphics.fill(x + 7, y + 1, x + 8, y + 4, color);
    graphics.fill(x + 1, y + 4, x + 9, y + ICON_SIZE, color);
    graphics.fill(x + 4, y + 6, x + 6, y + 8, 0xFF14161A);
  }

  public void renderOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    if (!open) {
      return;
    }
    graphics.nextStratum();
    int left = panelLeft();
    int top = panelTop();
    int right = left + panelW();
    int bottom = top + panelH();
    graphics.fill(left, top, right, bottom, 0xF8101014);
    graphics.fill(left, top, right, top + 1, 0xFFE8C43A);
    graphics.fill(left, bottom - 1, right, bottom, 0xFF6E747D);
    graphics.fill(left, top, left + 1, bottom, 0xFF6E747D);
    graphics.fill(right - 1, top, right, bottom, 0xFF6E747D);
    graphics.text(font, Component.translatable(key("label")), left + PAD, top + PAD, 0xFF8A8E94);
    box.setX(left + PAD);
    box.setY(top + PAD + LABEL_H);
    button.setX(left + PAD + FIELD_W + 6);
    button.setY(top + PAD + LABEL_H - 1);
    box.extractRenderState(graphics, mouseX, mouseY, partialTick);
    button.extractRenderState(graphics, mouseX, mouseY, partialTick);
  }

  public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (open || !overIcon(mouseX, mouseY)) {
      return;
    }
    List<FormattedCharSequence> lines = new ArrayList<>();
    lines.add(Component.translatable("tooltip." + Faktocraft.MODID + ".scan_code", current.get())
        .withStyle(ChatFormatting.LIGHT_PURPLE).getVisualOrderText());
    lines.addAll(font.split(Component.translatable(key("help")).withStyle(ChatFormatting.GRAY), TOOLTIP_W));
    graphics.setTooltipForNextFrame(font, lines, mouseX, mouseY);
  }

  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (overIcon(mouseX, mouseY)) {
      if (mouseButton == 0) {
        if (open) {
          close();
        } else {
          open = true;
          box.setValue(current.get());
          box.visible = true;
          button.visible = true;
        }
      }
      return true;
    }
    if (!open) {
      return false;
    }
    if (box.isFocused() && !box.isMouseOver(mouseX, mouseY)) {
      unfocus();
    }
    if (!overPanel(mouseX, mouseY)) {
      close();
      return true;
    }
    return false;
  }

  public boolean keyPressed(KeyEvent event) {

    int keyCode = event.key();
    if (box.isFocused()) {
      if (keyCode == InputConstants.KEY_ESCAPE) {
        unfocus();
        return true;
      }
      if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
        apply();
        return true;
      }
      if (box.keyPressed(event)) {
        return true;
      }
      return keyCode != InputConstants.KEY_TAB;
    }
    if (open && keyCode == InputConstants.KEY_ESCAPE) {
      close();
      return true;
    }
    return false;
  }
}
