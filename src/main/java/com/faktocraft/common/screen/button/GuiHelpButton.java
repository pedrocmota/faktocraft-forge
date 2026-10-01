package com.faktocraft.common.screen.button;

import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class GuiHelpButton extends Button {

  public GuiHelpButton(int x, int y, OnPress onPress, Component tooltip) {
    super(x, y, 12, 12, Component.empty(), onPress, DEFAULT_NARRATION);
    setTooltip(Tooltip.create(tooltip));
  }

  @Override
  protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    int line = isHoveredOrFocused() ? 0xFF101010 : 0xFF4A4A4A;
    int fill = isHoveredOrFocused() ? 0x50FFFFFF : 0x30FFFFFF;
    int x = getX();
    int y = getY();
    graphics.fill(x + 1, y + 1, x + 11, y + 11, fill);
    graphics.fill(x + 1, y + 1, x + 11, y + 2, line);
    graphics.fill(x + 1, y + 10, x + 11, y + 11, line);
    graphics.fill(x + 1, y + 1, x + 2, y + 11, line);
    graphics.fill(x + 10, y + 1, x + 11, y + 11, line);
    graphics.text(Minecraft.getInstance().font, "?", x + 4, y + 2, GuiUtil.opaque(line), false);
  }
}
