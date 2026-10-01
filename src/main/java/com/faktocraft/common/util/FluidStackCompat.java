package com.faktocraft.common.util;

import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidStackCompat {
  private FluidStackCompat() {
  }

  public static boolean isFluidEqual(FluidStack a, FluidStack b) {
    return FluidStack.isSameFluidSameComponents(a, b);
  }

  public static boolean containsFluid(FluidStack container, FluidStack other) {
    return isFluidEqual(container, other) && container.getAmount() >= other.getAmount();
  }
}
