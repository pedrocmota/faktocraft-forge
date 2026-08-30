package com.faktocraft.common.screen.bar;

import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.IndReb;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.progress.GuiProgress;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiFertilizerBar extends GuiProgress {

  public GuiFertilizerBar(IGuiWrapper wrapper, int leftOffset, int topOffset, IProgress progress) {
    super(wrapper, leftOffset, topOffset, progress, GuiSprite.FERTILIZER, Direction.HORIZONTAL, false);
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.COMMON;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.renderTooltip(GuiUtil.getFont(),
          Component.translatable("gui." + IndReb.MODID + ".waste", getProgress().getPercentProgressString() + "%"),
          mouseX, mouseY);
    }
  }
}
