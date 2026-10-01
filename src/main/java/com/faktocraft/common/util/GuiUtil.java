package com.faktocraft.common.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.function.Consumer;

public class GuiUtil {
  public static final int BUTTON_LEFT = 0;
  public static final int BUTTON_RIGHT = 1;
  public static final int BUTTON_MIDDLE = 2;

  public static int cyclingIndex(int size) {
    return size <= 1 ? 0 : (int) ((System.currentTimeMillis() / 1000L) % size);
  }

  public static Font getFont() {
    Minecraft minecraft = Minecraft.getInstance();
    return minecraft.font;
  }

  public static int legacyButton(MouseButtonEvent event) {
    return legacyButton(event.button());
  }

  public static int legacyButton(int button) {
    return switch (button) {
      case InputConstants.MOUSE_BUTTON_LEFT -> BUTTON_LEFT;
      case InputConstants.MOUSE_BUTTON_RIGHT -> BUTTON_RIGHT;
      case InputConstants.MOUSE_BUTTON_MIDDLE -> BUTTON_MIDDLE;
      default -> button;
    };
  }

  public static boolean hasShiftDown() {
    return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
  }

  public static boolean hasControlDown() {
    if (com.mojang.blaze3d.platform.MacosUtil.IS_MACOS) {
      return InputConstants.isKeyDown(InputConstants.KEY_LGUI) || InputConstants.isKeyDown(InputConstants.KEY_RGUI);
    }
    return InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
        || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
  }

  public static int opaque(int color) {
    return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
  }

  public static void drawString(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean shadow) {
    graphics.text(getFont(), text, x, y, opaque(color), shadow);
  }

  public static void prepTextScale(GuiGraphicsExtractor graphics, Consumer<GuiGraphicsExtractor> runnable, float x,
      float y, float scale) {
    float yAdd = 4 - (scale * 8) / 2F;
    graphics.pose().pushMatrix();
    graphics.pose().translate(x, y + yAdd);
    graphics.pose().scale(scale, scale);
    runnable.accept(graphics);
    graphics.pose().popMatrix();
  }

  public static void renderScaled(GuiGraphicsExtractor graphics, String text, int x, int y, float scale, int color,
      boolean shadow) {
    prepTextScale(graphics, g -> drawString(g, text, 0, 0, color, shadow), x, y, scale);
  }

  public static void renderScaledToFit(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth,
      int color) {
    int width = getFont().width(text);
    float scale = width <= maxWidth ? 1.0f : (float) maxWidth / width;
    renderScaled(graphics, text, x, y, scale, color, false);
  }
}
