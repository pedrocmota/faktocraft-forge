package com.faktocraft.common.screen.bar;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;

public class GuiFluidBarVertical extends GuiFluidBar {

  public GuiFluidBarVertical(IGuiWrapper wrapper, int leftOffset, int topOffset, FluidStorage fluidStorage) {
    super(wrapper, 16, 49, leftOffset, topOffset, fluidStorage, 0, 50);
  }
}
