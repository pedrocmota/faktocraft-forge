package com.faktocraft.common.screen.widgets;

import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class GuiText extends GuiElement {

  private final Component component;

  public GuiText(IGuiWrapper wrapper, int width, int height, int leftOffset, int topOffset, Component component) {
    super(wrapper, width, height, leftOffset, topOffset);
    this.component = component;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    float scale = 0.8f;
    int textWidth = minecraft.font.width(component.getString());
    if (textWidth * scale > getWidth()) {
      scale = (float) getWidth() / textWidth;
    }
    GuiUtil.renderScaled(graphics, component.getString(), getLeftOffset(), getTopOffset(), scale, 4210752, false);
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
