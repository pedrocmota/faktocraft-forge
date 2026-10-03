package com.faktocraft.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class GuiScaleHelper {
  private static final int MIN_SCALE = 1;

  private static boolean restoring;

  private GuiScaleHelper() {
  }

  public static void fit(Minecraft minecraft, Screen screen, int neededWidth, int neededHeight) {
    fit(minecraft, screen, neededWidth, neededHeight, 0);
  }

  public static void fit(Minecraft minecraft, Screen screen, int neededWidth, int neededHeight,
      int comfortMargin) {
    if (restoring) {
      return;
    }
    Window window = minecraft.getWindow();
    int preferred = window.calculateScale(minecraft.options.guiScale().get(), minecraft.isEnforceUnicode());
    int scale = preferred;
    while (scale > MIN_SCALE && !fits(window, scale, neededWidth, neededHeight)) {
      scale--;
    }

    if (comfortMargin > 0 && scale >= 3
        && !fits(window, scale, neededWidth, neededHeight + comfortMargin)) {
      scale--;
    }
    apply(minecraft, screen, scale);
  }

  private static void apply(Minecraft minecraft, Screen screen, int scale) {
    Window window = minecraft.getWindow();
    if (window.getGuiScale() == scale) {
      return;
    }
    window.setGuiScale(scale);
    net.neoforged.neoforge.client.ClientHooks.fireWindowResize(window);
    minecraft.gui.resizeScreenLayers(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    minecraft.mouseHandler.setIgnoreFirstMove();
    screen.width = window.getGuiScaledWidth();
    screen.height = window.getGuiScaledHeight();
  }

  private static boolean fits(Window window, int scale, int neededWidth, int neededHeight) {
    return window.getWidth() / scale >= neededWidth && window.getHeight() / scale >= neededHeight;
  }

  public static void restore(Minecraft minecraft) {
    Window window = minecraft.getWindow();
    int preferred = window.calculateScale(minecraft.options.guiScale().get(), minecraft.isEnforceUnicode());
    if (window.getGuiScale() != preferred) {
      restoring = true;
      try {
        minecraft.resizeGui();
      } finally {
        restoring = false;
      }
    }
  }
}
