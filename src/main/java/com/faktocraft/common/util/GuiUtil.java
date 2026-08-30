package com.faktocraft.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.util.function.Consumer;

public class GuiUtil {

  public static int cyclingIndex(int size) {
    return size <= 1 ? 0 : (int) ((System.currentTimeMillis() / 1000L) % size);
  }

  public static Font getFont() {
    Minecraft minecraft = Minecraft.getInstance();
    return minecraft.font;
  }

  public static void drawString(GuiGraphics graphics, String text, int x, int y, int color, boolean shadow) {
    if ((color & 0xFF000000) == 0) {
      color |= 0xFF000000;
    }
    graphics.drawString(getFont(), text, x, y, color, shadow);
  }

  public static void prepTextScale(GuiGraphics graphics, Consumer<GuiGraphics> runnable, float x,
      float y, float scale) {
    float yAdd = 4 - (scale * 8) / 2F;
    graphics.pose().pushPose();
    graphics.pose().translate(x, y + yAdd, 0);
    graphics.pose().scale(scale, scale, 1.0F);
    runnable.accept(graphics);
    graphics.pose().popPose();
  }

  public static void renderScaled(GuiGraphics graphics, String text, int x, int y, float scale, int color,
      boolean shadow) {
    prepTextScale(graphics, g -> drawString(g, text, 0, 0, color, shadow), x, y, scale);
  }

  public static void renderScaledToFit(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
    int width = getFont().width(text);
    float scale = width <= maxWidth ? 1.0f : (float) maxWidth / width;
    renderScaled(graphics, text, x, y, scale, color, false);
  }
}
