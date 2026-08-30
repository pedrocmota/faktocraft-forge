package com.faktocraft.common.screen.bar;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;

public class GuiFluidBarVerticalLarge extends GuiFluidBar {

  public GuiFluidBarVerticalLarge(IGuiWrapper wrapper, int leftOffset, int topOffset, FluidStorage fluidStorage) {
    super(wrapper, 36, 49, leftOffset, topOffset, fluidStorage, 36, 50);
  }
}
