package com.faktocraft.client;

import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class DeviceButton extends Button {

  public DeviceButton(int x, int y, int width, int height, Component message, OnPress onPress) {
    super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
  }

  @Override
  public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    int x0 = getX();
    int y0 = getY();
    int x1 = x0 + width;
    int y1 = y0 + height;
    boolean hover = active && isHoveredOrFocused();
    int fill = !active ? 0xFF17191D : hover ? 0xFF262A30 : 0xFF1E2126;
    int border = !active ? 0xFF3A3E44 : hover ? 0xFFE8C43A : 0xFF6E747D;
    int text = !active ? 0xFF6A6E74 : hover ? 0xFFE8C43A : 0xFFE8E8E8;
    graphics.fill(x0, y0, x1, y1, fill);
    graphics.fill(x0, y0, x1, y0 + 1, border);
    graphics.fill(x0, y1 - 1, x1, y1, border);
    graphics.fill(x0, y0, x0 + 1, y1, border);
    graphics.fill(x1 - 1, y0, x1, y1, border);
    graphics.centeredText(net.minecraft.client.Minecraft.getInstance().font, getMessage(),
        (x0 + x1) / 2, y0 + (height - 8) / 2, GuiUtil.opaque(text));
  }
}
