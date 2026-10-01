package com.faktocraft.common.screen.button;

import net.minecraft.client.Minecraft;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

public class GuiBackwardButton extends GuiButton {

  List<Component> tooltip;

  public GuiBackwardButton(IGuiWrapper wrapper, int leftOffset, int topOffset, Runnable leftClick,
      List<Component> tooltip) {
    super(wrapper, leftOffset, topOffset, GuiSprite.SMALL_BUTTON, leftClick, null);
    this.tooltip = tooltip;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    GuiSprite sprite = GuiSprite.BACKWARD_ICON;
    blit(graphics, getLeftOffset() + sprite.getRenderOffsetLeft(), getTopOffset() + sprite.getRenderOffsetTop(),
        sprite.getOffsetLeft(), sprite.getOffsetTop(), sprite.getWidth(), sprite.getHeight());
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      if (tooltip != null) {
        graphics.setComponentTooltipForNextFrame(GuiUtil.getFont(), tooltip, mouseX, mouseY);
      }
    }
  }
}
