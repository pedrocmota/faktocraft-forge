package com.faktocraft.common.item.base;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidItemHandlerProvider implements ICapabilityProvider, IFluidHandlerItem {

  private final ItemStack stack;
  private final int capacityMb;
  private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);

  public FluidItemHandlerProvider(ItemStack stack, int capacityMb) {
    this.stack = stack;
    this.capacityMb = capacityMb;
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    return cap == ForgeCapabilities.FLUID_HANDLER_ITEM ? holder.cast() : LazyOptional.empty();
  }

  @NotNull
  @Override
  public ItemStack getContainer() {
    return stack;
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @NotNull
  @Override
  public FluidStack getFluidInTank(int tank) {
    Fluid fluid = FluidItem.getFluid(stack);
    int amount = FluidItem.getFluidAmount(stack);
    return fluid == Fluids.EMPTY || amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
  }

  @Override
  public int getTankCapacity(int tank) {
    return capacityMb;
  }

  @Override
  public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
    return true;
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {

    if (resource.isEmpty() || stack.getCount() != 1) {
      return 0;
    }
    FluidStack current = getFluidInTank(0);
    if (!current.isEmpty() && !current.isFluidEqual(resource)) {
      return 0;
    }
    int filled = Math.min(capacityMb - current.getAmount(), resource.getAmount());
    if (filled > 0 && action.execute()) {
      FluidItem.setFluid(stack, resource.getFluid(), current.getAmount() + filled);
    }
    return Math.max(0, filled);
  }

  @NotNull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    FluidStack current = getFluidInTank(0);
    if (resource.isEmpty() || current.isEmpty() || !current.isFluidEqual(resource)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @NotNull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    FluidStack current = getFluidInTank(0);
    if (current.isEmpty() || maxDrain <= 0 || stack.getCount() != 1) {
      return FluidStack.EMPTY;
    }
    int drained = Math.min(current.getAmount(), maxDrain);
    if (action.execute()) {
      FluidItem.setFluid(stack, current.getFluid(), current.getAmount() - drained);
    }
    return new FluidStack(current.getFluid(), drained);
  }
}
