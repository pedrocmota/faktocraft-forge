package com.faktocraft.common.util.transfer;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public interface IFluidHandler {
  enum FluidAction {
    EXECUTE, SIMULATE;

    public boolean execute() {
      return this == EXECUTE;
    }

    public boolean simulate() {
      return this == SIMULATE;
    }
  }

  int getTanks();

  @NotNull
  FluidStack getFluidInTank(int tank);

  int getTankCapacity(int tank);

  boolean isFluidValid(int tank, @NotNull FluidStack stack);

  int fill(FluidStack resource, FluidAction action);

  @NotNull
  FluidStack drain(FluidStack resource, FluidAction action);

  @NotNull
  FluidStack drain(int maxDrain, FluidAction action);

  default void setFluidInTank(int tank, @NotNull FluidStack stack) {
    FluidStack current = getFluidInTank(tank);
    if (!current.isEmpty()) {
      drain(current.copy(), FluidAction.EXECUTE);
    }
    if (!stack.isEmpty()) {
      fill(stack.copy(), FluidAction.EXECUTE);
    }
  }
}
