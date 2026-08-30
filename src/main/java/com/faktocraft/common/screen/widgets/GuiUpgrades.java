package com.faktocraft.common.screen.widgets;

import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiUpgrades extends GuiElement {

  public GuiUpgrades(IGuiWrapper wrapper) {
    super(wrapper, 24, 80, 175, 4);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    blit(graphics, getLeftOffset(), getTopOffset(), 0, 134, getWidth(), getHeight());
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.COMMON;
  }
}
