package com.faktocraft.common.entity.block;

import com.faktocraft.common.interfaces.entity.IProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import java.util.function.Predicate;

public class FluidStorage implements IFluidHandler, IProgress {

  private final int capacityMb;
  private final Predicate<FluidStack> validator;
  private boolean outputOnly = false;
  private FluidStack fluid = FluidStack.EMPTY;

  private Runnable changeListener = () -> {
  };

  public FluidStorage(int capacityMb) {
    this(capacityMb, v -> true);
  }

  public FluidStorage(int capacityMb, Predicate<FluidStack> validator) {
    this.capacityMb = capacityMb;
    this.validator = validator;
  }

  public void setChangeListener(Runnable listener) {
    this.changeListener = listener;
  }

  public FluidStorage markOutputOnly() {
    this.outputOnly = true;
    return this;
  }

  public boolean isOutputOnly() {
    return outputOnly;
  }

  public Fluid getFluid() {
    return fluid.getFluid();
  }

  public FluidStack getFluidStack() {
    return fluid;
  }

  public int getFluidAmount() {
    return fluid.getAmount();
  }

  public int getCapacityMb() {
    return capacityMb;
  }

  public boolean isEmpty() {
    return fluid.isEmpty();
  }

  public boolean isValid(FluidStack stack) {
    return validator.test(stack);
  }

  public int fillFluid(FluidStack fillFluid, int amountMb, boolean simulate) {
    if (fillFluid.isEmpty() || amountMb <= 0 || !validator.test(fillFluid)) {
      return 0;
    }
    if (!fluid.isEmpty() && !fluid.isFluidEqual(fillFluid)) {
      return 0;
    }

    int space = capacityMb - fluid.getAmount();
    int filled = Math.min(space, amountMb);
    if (filled > 0 && !simulate) {
      if (fluid.isEmpty()) {
        fluid = new FluidStack(fillFluid, filled);
      } else {
        fluid.grow(filled);
      }
      onChanged();
    }
    return filled;
  }

  public int takeFluid(int amountMb, boolean simulate) {
    int taken = Math.min(fluid.getAmount(), amountMb);
    if (taken > 0 && !simulate) {
      fluid.shrink(taken);
      if (fluid.getAmount() <= 0) {
        fluid = FluidStack.EMPTY;
      }
      onChanged();
    }
    return taken;
  }

  public void setFluid(FluidStack stack, int amountMb) {
    int clamped = Math.max(0, Math.min(amountMb, capacityMb));
    this.fluid = (clamped > 0 && !stack.isEmpty()) ? new FluidStack(stack, clamped) : FluidStack.EMPTY;
    onChanged();
  }

  private void onChanged() {
    changeListener.run();
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @NotNull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return fluid;
  }

  @Override
  public int getTankCapacity(int tank) {
    return capacityMb;
  }

  @Override
  public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
    return !outputOnly && validator.test(stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (outputOnly || resource.isEmpty()) {
      return 0;
    }
    return fillFluid(resource, resource.getAmount(), action.simulate());
  }

  @NotNull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || fluid.isEmpty() || !fluid.isFluidEqual(resource)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @NotNull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    if (fluid.isEmpty()) {
      return FluidStack.EMPTY;
    }
    FluidStack drainedFluid = new FluidStack(fluid, Math.min(fluid.getAmount(), maxDrain));
    int taken = takeFluid(maxDrain, action.simulate());
    if (taken <= 0) {
      return FluidStack.EMPTY;
    }
    drainedFluid.setAmount(taken);
    return drainedFluid;
  }

  @Override
  public float getProgress() {
    return fluid.getAmount();
  }

  @Override
  public float getProgressMax() {
    return capacityMb;
  }

  public void save(CompoundTag tag) {
    CompoundTag variantTag = new CompoundTag();
    if (!fluid.isEmpty()) {
      fluid.writeToNBT(variantTag);
    }
    tag.put("variant", variantTag);
    tag.putInt("amount", fluid.getAmount());
  }

  public void load(CompoundTag tag) {
    FluidStack loaded = tag.contains("variant")
        ? FluidStack.loadFluidStackFromNBT(tag.getCompound("variant"))
        : FluidStack.EMPTY;
    int amountMb = tag.contains("amount") ? tag.getInt("amount") : 0;
    if (loaded.isEmpty() || amountMb <= 0) {
      this.fluid = FluidStack.EMPTY;
    } else {
      this.fluid = new FluidStack(loaded, amountMb);
    }
  }
}
