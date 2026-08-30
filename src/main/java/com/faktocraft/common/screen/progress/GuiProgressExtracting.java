package com.faktocraft.common.screen.progress;

import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;

public class GuiProgressExtracting extends GuiProgress {
  public GuiProgressExtracting(IGuiWrapper wrapper, int leftOffset, int topOffset, IProgress progress) {
    super(wrapper, leftOffset, topOffset, progress, GuiSprite.FLUID_EXTRACTING, Direction.HORIZONTAL, false);
  }
}
