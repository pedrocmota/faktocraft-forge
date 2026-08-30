package com.faktocraft.common.screen.button;

import net.minecraft.client.Minecraft;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

public class GuiForwardButton extends GuiButton {

  List<Component> tooltip;

  public GuiForwardButton(IGuiWrapper wrapper, int leftOffset, int topOffset, Runnable leftClick,
      List<Component> tooltip) {
    super(wrapper, leftOffset, topOffset, GuiSprite.SMALL_BUTTON, leftClick, null);
    this.tooltip = tooltip;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    GuiSprite sprite = GuiSprite.FORWARD_ICON;
    blit(graphics, getLeftOffset() + sprite.getRenderOffsetLeft(), getTopOffset() + sprite.getRenderOffsetTop(),
        sprite.getOffsetLeft(), sprite.getOffsetTop(), sprite.getWidth(), sprite.getHeight());
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      if (tooltip != null) {
        graphics.renderComponentTooltip(GuiUtil.getFont(), tooltip, mouseX, mouseY);
      }
    }
  }
}
