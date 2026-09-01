package com.faktocraft.common.screen.widgets;

import net.minecraft.client.Minecraft;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.interfaces.entity.ICooldown;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiCooldown extends GuiElement {

  private final ICooldown be = (ICooldown) getWrapper().getBlockEntity();

  public GuiCooldown(IGuiWrapper wrapper) {
    super(wrapper, 20, 16, 156, -15);
  }

  private boolean bufferFull() {
    var storage = getWrapper().getBlockEntity().getEnergyStorage();
    return storage != null && storage.energyStored() >= storage.maxEnergy();
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      if (be.getCooldown() > 0) {
        graphics.renderTooltip(GuiUtil.getFont(),
            Component.translatable("gui." + Faktocraft.MODID + ".can_operate_in", be.getCooldown()), mouseX, mouseY);
      } else if (bufferFull()) {
        graphics.renderTooltip(GuiUtil.getFont(),
            Component.translatable("gui." + Faktocraft.MODID + ".buffer_full"), mouseX, mouseY);
      } else {
        graphics.renderTooltip(GuiUtil.getFont(),
            Component.translatable("gui." + Faktocraft.MODID + ".can_operate"), mouseX, mouseY);
      }
    }
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    if (be.getCooldown() == 0 && !bufferFull()) {
      blit(graphics, getLeftOffset(), getTopOffset(), 0, 100, getWidth(), getHeight());
    } else {
      blit(graphics, getLeftOffset(), getTopOffset(), 0, 117, getWidth(), getHeight());
    }

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.COMMON;
  }
}
