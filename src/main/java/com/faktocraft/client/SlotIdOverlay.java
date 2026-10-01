package com.faktocraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class SlotIdOverlay {
  private static boolean enabled;

  private SlotIdOverlay() {
  }

  public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event, KeyMapping mapping) {
    if (mapping == null || !(event.getScreen() instanceof AbstractContainerScreen<?>)) {
      return;
    }

    if (event.getScreen().getFocused() instanceof EditBox box && box.canConsumeInput()) {
      return;
    }
    if (mapping.isActiveAndMatches(InputConstants.getKey(event.getKeyEvent()))) {
      enabled = !enabled;
      event.setCanceled(true);
    }
  }

  public static void onRender(ScreenEvent.Render.Foreground event) {
    if (!enabled || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
      return;
    }
    Font font = Minecraft.getInstance().font;
    GuiGraphicsExtractor graphics = event.getGuiGraphics();
    int left = screen.getLeftPos();
    int top = screen.getTopPos();

    for (Slot slot : screen.getMenu().slots) {
      if (!slot.isActive()) {
        continue;
      }
      int x = left + slot.x;
      int y = top + slot.y;
      graphics.fill(x, y, x + 16, y + 16, 0xB0000000);
      String text = String.valueOf(slot.getSlotIndex());

      float scale = text.length() > 2 ? 0.5f : 1.0f;
      graphics.pose().pushMatrix();
      graphics.pose().translate(x + 8 - font.width(text) * scale / 2f, y + 8 - 4 * scale);
      graphics.pose().scale(scale, scale);
      graphics.text(font, text, 0, 0, 0xFFFFFF00, true);
      graphics.pose().popMatrix();
    }
  }

  public static void reset() {
    enabled = false;
  }
}
