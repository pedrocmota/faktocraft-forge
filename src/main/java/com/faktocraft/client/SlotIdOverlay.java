package com.faktocraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.client.event.ScreenEvent;

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
    if (mapping.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
      enabled = !enabled;
      event.setCanceled(true);
    }
  }

  public static void onRender(ScreenEvent.Render.Post event) {
    if (!enabled || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
      return;
    }
    Font font = Minecraft.getInstance().font;
    GuiGraphics graphics = event.getGuiGraphics();
    int left = screen.getGuiLeft();
    int top = screen.getGuiTop();
    graphics.pose().pushPose();

    graphics.pose().translate(0, 0, 400);
    for (Slot slot : screen.getMenu().slots) {
      if (!slot.isActive()) {
        continue;
      }
      int x = left + slot.x;
      int y = top + slot.y;
      graphics.fill(x, y, x + 16, y + 16, 0xB0000000);
      String text = String.valueOf(slot.getSlotIndex());

      float scale = text.length() > 2 ? 0.5f : 1.0f;
      graphics.pose().pushPose();
      graphics.pose().translate(x + 8 - font.width(text) * scale / 2f, y + 8 - 4 * scale, 0);
      graphics.pose().scale(scale, scale, 1.0f);
      graphics.drawString(font, text, 0, 0, 0xFFFFFF00, true);
      graphics.pose().popPose();
    }
    graphics.pose().popPose();
  }

  public static void reset() {
    enabled = false;
  }
}
