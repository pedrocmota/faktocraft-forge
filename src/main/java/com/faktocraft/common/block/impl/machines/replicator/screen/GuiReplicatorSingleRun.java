package com.faktocraft.common.block.impl.machines.replicator.screen;

import com.faktocraft.common.block.impl.machines.replicator.BlockEntityReplicator;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.button.GuiButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class GuiReplicatorSingleRun extends GuiButton {

  public GuiReplicatorSingleRun(IGuiWrapper wrapper, BlockEntityReplicator blockEntityReplicator, Runnable leftClick) {
    super(wrapper, 105, 61, GuiSprite.REPLICATOR_SINGLE, leftClick, null);
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.setTooltipForNextFrame(com.faktocraft.common.util.GuiUtil.getFont(),
          EnumLang.SINGLE_RUN.getTranslationComponent(), mouseX, mouseY);
    }
  }
}
