package com.faktocraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class TeleportFxOverlay implements IGuiOverlay {

  public static final String ID = "teleport_fx";

  private static final long FLASH_IN_MS = 120;
  private static final long FADE_OUT_MS = 900;
  private static final long TOTAL_MS = FLASH_IN_MS + FADE_OUT_MS;

  private static final int GLOW_RGB = 0x2FD8E8;
  private static final int CORE_RGB = 0xEAFBFF;

  private static long fxStart = Long.MIN_VALUE;

  public static void trigger() {
    fxStart = Util.getMillis();
  }

  @Override
  public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
    long elapsed = Util.getMillis() - fxStart;
    if (elapsed < 0 || elapsed > TOTAL_MS) {
      return;
    }

    float glowAlpha;
    float coreAlpha;
    if (elapsed <= FLASH_IN_MS) {
      float t = elapsed / (float) FLASH_IN_MS;
      glowAlpha = 0.85F * t;
      coreAlpha = 0.95F * t;
    } else {
      float t = (elapsed - FLASH_IN_MS) / (float) FADE_OUT_MS;
      float ease = 1F - (t * t);
      glowAlpha = 0.85F * ease;
      coreAlpha = 0.95F * Math.max(0F, 1F - (t * 2.2F));
    }

    graphics.fill(0, 0, screenWidth, screenHeight, withAlpha(GLOW_RGB, glowAlpha));
    if (coreAlpha > 0) {
      graphics.fill(0, 0, screenWidth, screenHeight, withAlpha(CORE_RGB, coreAlpha));
    }
  }

  private static int withAlpha(int rgb, float alpha) {
    int a = Mth.clamp((int) (alpha * 255F), 0, 255);
    return (a << 24) | (rgb & 0xFFFFFF);
  }
}
