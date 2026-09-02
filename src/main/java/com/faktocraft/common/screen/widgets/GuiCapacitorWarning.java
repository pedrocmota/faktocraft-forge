package com.faktocraft.common.screen.widgets;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiCapacitorWarning extends GuiElement {

  private static final int WARN_BG = 0xFF3B3B3B;
  private static final int WARN_BORDER = 0xFFD8433B;
  private static final int WARN_MARK = 0xFFFFD75E;

  private final FaktocraftBlockEntity blockEntity;

  public GuiCapacitorWarning(IGuiWrapper wrapper, int leftOffset, int topOffset, FaktocraftBlockEntity blockEntity) {
    super(wrapper, 18, 49, leftOffset, topOffset);
    this.blockEntity = blockEntity;
  }

  private boolean active() {
    return blockEntity.hasBatteryDock() && blockEntity.getBatteryDockCapacity() <= 0;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    if (!active()) {
      return;
    }
    int l = getLeftOffset();
    int t = getTopOffset();
    graphics.fill(l, t, l + getWidth(), t + getHeight(), WARN_BORDER);
    graphics.fill(l + 1, t + 1, l + getWidth() - 1, t + getHeight() - 1, WARN_BG);
    if ((System.currentTimeMillis() / 600) % 2 == 0) {
      int cx = l + getWidth() / 2;
      int cy = t + getHeight() / 2;
      graphics.fill(cx - 1, cy - 12, cx + 1, cy + 4, WARN_MARK);
      graphics.fill(cx - 1, cy + 8, cx + 1, cy + 10, WARN_MARK);
    }
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (active() && isMouseOver(mouseX, mouseY)) {
      graphics.renderTooltip(GuiUtil.getFont(),
          Component.translatable("gui." + Faktocraft.MODID + ".capacitor_required")
              .withStyle(ChatFormatting.RED),
          mouseX, mouseY);
    }
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.COMMON;
  }
}
