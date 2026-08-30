package com.faktocraft.common.block.impl.machines;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.impl.FluidCell;
import com.faktocraft.common.util.ItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class FluidCellTankHelper {

  private FluidCellTankHelper() {
  }

  private static int getCellCapacity(ItemStack stack) {
    return stack.getItem() instanceof FluidCell ? FluidCell.getCapacity() : 1000;
  }

  public static boolean canReceiveFluid(ItemStack stack, Fluid fluid) {
    if (stack.isEmpty() || !(stack.getItem() instanceof FluidItem)) {
      return false;
    }
    Fluid stored = FluidItem.getFluid(stack);
    if (stored == Fluids.EMPTY) {
      return true;
    }
    return stored == fluid && FluidItem.getFluidAmount(stack) < getCellCapacity(stack);
  }

  public static boolean hasEnoughToFill(ItemStack stack, FluidStorage tank) {
    if (tank.isEmpty() || !canReceiveFluid(stack, tank.getFluid())) {
      return false;
    }
    int amount = Math.min(1000, getCellCapacity(stack) - FluidItem.getFluidAmount(stack));
    return amount > 0 && tank.getFluidAmount() >= amount;
  }

  public static boolean drainToCell(ItemStackHandler handler, int slotUp, int slotDown, FluidStorage tank) {
    ItemStack up = handler.getStackInSlot(slotUp);
    ItemStack down = handler.getStackInSlot(slotDown);

    if (up.isEmpty() || !(up.getItem() instanceof FluidItem)) {
      return false;
    }
    if (!down.isEmpty() && down.getCount() + 1 > down.getMaxStackSize()) {
      return false;
    }
    if (tank.isEmpty()) {
      return false;
    }

    Fluid tankFluid = tank.getFluid();
    Fluid cellFluid = FluidItem.getFluid(up);
    if (cellFluid != Fluids.EMPTY && cellFluid != tankFluid) {
      return false;
    }

    int capacity = getCellCapacity(up);
    int current = FluidItem.getFluidAmount(up);
    int amount = Math.min(1000, capacity - current);
    if (amount <= 0 || tank.getFluidAmount() < amount) {
      return false;
    }

    if (current + amount < capacity && up.getCount() > 1) {
      return false;
    }

    ItemStack filled = up.copy();
    filled.setCount(1);
    FluidItem.setFluid(filled, tankFluid, current + amount);
    tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);

    if (current + amount >= capacity) {
      ItemStack remaining = up.copy();
      remaining.shrink(1);
      handler.setStackInSlot(slotUp, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
      if (down.isEmpty()) {
        handler.setStackInSlot(slotDown, filled);
      } else if (ItemStack.isSameItemSameTags(down, filled)) {
        down.grow(1);
        handler.setStackInSlot(slotDown, down);
      } else {
        tank.fill(new FluidStack(tankFluid, amount), IFluidHandler.FluidAction.EXECUTE);
        return false;
      }
    } else {
      handler.setStackInSlot(slotUp, filled);
    }
    return true;
  }

  public static boolean fillFromCell(ItemStackHandler handler, int slotUp, int slotDown, FluidStorage tank) {
    ItemStack up = handler.getStackInSlot(slotUp);
    ItemStack down = handler.getStackInSlot(slotDown);

    if (up.isEmpty() || !(up.getItem() instanceof FluidItem)) {
      return false;
    }
    if (!down.isEmpty() && down.getCount() + 1 > down.getMaxStackSize()) {
      return false;
    }

    Fluid cellFluid = FluidItem.getFluid(up);
    int amount = FluidItem.getFluidAmount(up);
    if (cellFluid == Fluids.EMPTY || amount <= 0) {
      return false;
    }

    FluidStack cellStack = new FluidStack(cellFluid, amount);
    if (tank.fill(cellStack, IFluidHandler.FluidAction.SIMULATE) != amount) {
      return false;
    }

    ItemStack emptied = up.copy();
    emptied.setCount(1);
    FluidItem.setFluid(emptied, Fluids.EMPTY, 0);

    if (!down.isEmpty() && !ItemStack.isSameItemSameTags(down, emptied)) {
      return false;
    }

    tank.fill(cellStack, IFluidHandler.FluidAction.EXECUTE);

    ItemStack remaining = up.copy();
    remaining.shrink(1);
    handler.setStackInSlot(slotUp, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    if (down.isEmpty()) {
      handler.setStackInSlot(slotDown, emptied);
    } else {
      down.grow(1);
      handler.setStackInSlot(slotDown, down);
    }
    return true;
  }
}
