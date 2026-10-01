package com.faktocraft.common.screen.button;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class GuiCopyPasteButton extends Button {

  private final boolean paste;

  public GuiCopyPasteButton(int x, int y, boolean paste, OnPress onPress, Component tooltip) {
    super(x, y, 12, 12, Component.empty(), onPress, DEFAULT_NARRATION);
    this.paste = paste;
    setTooltip(Tooltip.create(tooltip));
  }

  private static void outline(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
    graphics.fill(x0, y0, x1, y0 + 1, color);
    graphics.fill(x0, y1 - 1, x1, y1, color);
    graphics.fill(x0, y0, x0 + 1, y1, color);
    graphics.fill(x1 - 1, y0, x1, y1, color);
  }

  @Override
  protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    drawIcon(graphics, getX(), getY(), paste, isHoveredOrFocused());
  }

  public static void drawIcon(GuiGraphicsExtractor graphics, int x, int y, boolean paste, boolean highlighted) {
    int line = highlighted ? 0xFF101010 : 0xFF4A4A4A;
    int fill = highlighted ? 0x50FFFFFF : 0x30FFFFFF;
    if (paste) {

      outline(graphics, x + 2, y + 2, x + 10, y + 11, line);
      graphics.fill(x + 3, y + 3, x + 9, y + 10, fill);
      graphics.fill(x + 4, y + 1, x + 8, y + 3, line);
    } else {

      outline(graphics, x + 1, y + 1, x + 8, y + 8, line);
      graphics.fill(x + 4, y + 4, x + 10, y + 11, fill);
      outline(graphics, x + 4, y + 4, x + 11, y + 11, line);
    }
  }
}
