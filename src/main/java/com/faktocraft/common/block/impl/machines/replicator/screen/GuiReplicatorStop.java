package com.faktocraft.common.block.impl.machines.replicator.screen;

import com.faktocraft.common.block.impl.machines.replicator.BlockEntityReplicator;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.button.GuiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class GuiReplicatorStop extends GuiButton {

  public GuiReplicatorStop(IGuiWrapper wrapper, BlockEntityReplicator blockEntityReplicator, Runnable leftClick) {
    super(wrapper, 86, 61, GuiSprite.REPLICATOR_STOP, leftClick, null);
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.renderTooltip(com.faktocraft.common.util.GuiUtil.getFont(),
          EnumLang.STOP_REPLICATION.getTranslationComponent(), mouseX,
          mouseY);
    }
  }
}
