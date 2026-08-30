package com.faktocraft.common.screen.progress;

import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;

public class GuiProgressRecycler extends GuiProgress {
  public GuiProgressRecycler(IGuiWrapper wrapper, int leftOffset, int topOffset, IProgress progress) {
    super(wrapper, leftOffset, topOffset, progress, GuiSprite.RECYCLER, Direction.HORIZONTAL, false);
  }
}
