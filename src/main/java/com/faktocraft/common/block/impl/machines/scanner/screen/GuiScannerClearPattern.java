package com.faktocraft.common.block.impl.machines.scanner.screen;

import com.faktocraft.common.block.impl.machines.scanner.BlockEntityScanner;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.button.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class GuiScannerClearPattern extends GuiButton {

  private final BlockEntityScanner entity;

  public GuiScannerClearPattern(IGuiWrapper wrapper, int leftOffset, int topOffset,
      BlockEntityScanner blockEntityScanner, Runnable leftClick) {
    super(wrapper, leftOffset, topOffset, GuiSprite.SCANNER_CLEAR, leftClick, null);
    this.entity = blockEntityScanner;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    if (entity.getMode().getId() >= 4) {
      super.renderBg(graphics, minecraft, mouseX, mouseY);
    }
  }

  @Override
  protected boolean onLeftClick() {
    if (entity.getMode().getId() >= 4) {
      super.onLeftClick();
    }
    return false;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY) && entity.getMode().getId() >= 4) {
      graphics.setTooltipForNextFrame(com.faktocraft.common.util.GuiUtil.getFont(),
          EnumLang.CLEAR_PATTERN.getTranslationComponent(), mouseX,
          mouseY);
    }
  }
}
