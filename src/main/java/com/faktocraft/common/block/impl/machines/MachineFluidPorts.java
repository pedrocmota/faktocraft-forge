package com.faktocraft.common.block.impl.machines;

import com.faktocraft.common.entity.block.FluidStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class MachineFluidPorts implements IFluidHandler {

  private final List<FluidStorage> inputs;
  private final List<FluidStorage> outputs;

  public MachineFluidPorts(List<FluidStorage> inputs, List<FluidStorage> outputs) {
    this.inputs = List.copyOf(inputs);
    this.outputs = List.copyOf(outputs);
  }

  private FluidStorage tank(int index) {
    return index < inputs.size() ? inputs.get(index) : outputs.get(index - inputs.size());
  }

  @Override
  public int getTanks() {
    return inputs.size() + outputs.size();
  }

  @NotNull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return tank(tank).getFluidStack();
  }

  @Override
  public int getTankCapacity(int tank) {
    return tank(tank).getCapacityMb();
  }

  @Override
  public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
    return tank < inputs.size() && inputs.get(tank).isValid(stack);
  }

  @Nullable
  private FluidStorage inputFor(FluidStack resource) {
    for (FluidStorage tank : inputs) {
      if (tank.isValid(resource) && !tank.isEmpty()
          && tank.getFluidStack().isFluidEqual(resource)
          && tank.getFluidAmount() < tank.getCapacityMb()) {
        return tank;
      }
    }
    for (FluidStorage tank : inputs) {
      if (tank.isValid(resource) && tank.isEmpty()) {
        return tank;
      }
    }
    return null;
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    FluidStorage target = inputFor(resource);
    return target == null ? 0 : target.fillFluid(resource, resource.getAmount(), action.simulate());
  }

  @NotNull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return FluidStack.EMPTY;
    }
    for (FluidStorage tank : outputs) {
      if (!tank.isEmpty() && tank.getFluidStack().isFluidEqual(resource)) {
        return tank.drain(resource, action);
      }
    }
    return FluidStack.EMPTY;
  }

  @NotNull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    for (FluidStorage tank : outputs) {
      if (!tank.isEmpty()) {
        return tank.drain(maxDrain, action);
      }
    }
    return FluidStack.EMPTY;
  }
}
