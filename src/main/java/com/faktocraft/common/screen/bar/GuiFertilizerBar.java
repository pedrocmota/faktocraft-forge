package com.faktocraft.common.screen.bar;

import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.progress.GuiProgress;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiFertilizerBar extends GuiProgress {

  public GuiFertilizerBar(IGuiWrapper wrapper, int leftOffset, int topOffset, IProgress progress) {
    super(wrapper, leftOffset, topOffset, progress, GuiSprite.FERTILIZER, Direction.HORIZONTAL, false);
  }

  @Override
  public Identifier getResourceLocation() {
    return Constants.COMMON;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.setTooltipForNextFrame(GuiUtil.getFont(),
          Component.translatable("gui." + Faktocraft.MODID + ".waste", getProgress().getPercentProgressString() + "%"),
          mouseX, mouseY);
    }
  }
}
