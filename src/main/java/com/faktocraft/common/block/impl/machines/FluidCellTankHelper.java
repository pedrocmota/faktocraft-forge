package com.faktocraft.common.block.impl.machines;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.impl.FluidCell;
import com.faktocraft.common.util.FluidInteractionHelper;
import com.faktocraft.common.util.ItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class FluidCellTankHelper {

  public static final int BOTTLE_MB = 250;

  private FluidCellTankHelper() {
  }

  public static boolean isEmptyBottle(ItemStack stack) {
    return stack.is(Items.GLASS_BOTTLE);
  }

  public static boolean isContainer(ItemStack stack) {
    return !isEmptyBottle(stack) && FluidInteractionHelper.isContainer(stack);
  }

  public static boolean acceptsInput(ItemStack stack) {
    return stack.getItem() instanceof FluidItem || isEmptyBottle(stack) || isContainer(stack);
  }

  private static boolean moveContainer(ItemStackHandler handler, int slotUp, int slotDown, FluidStorage tank,
      boolean fill) {
    ItemStack up = handler.getStackInSlot(slotUp);
    ItemStack down = handler.getStackInSlot(slotDown);
    if (fill ? !canDrainToCell(up, down, tank) : !canFillFromCell(up, down, tank)) {
      return false;
    }
    FluidActionResult result = fill
        ? FluidUtil.tryFillContainer(up, tank, Integer.MAX_VALUE, null, true)
        : FluidUtil.tryEmptyContainer(up, tank, Integer.MAX_VALUE, null, true);
    if (!result.isSuccess()) {
      return false;
    }
    ItemStack remaining = up.copy();
    remaining.shrink(1);
    handler.setStackInSlot(slotUp, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    if (down.isEmpty()) {
      handler.setStackInSlot(slotDown, result.getResult());
    } else {
      down.grow(1);
      handler.setStackInSlot(slotDown, down);
    }
    return true;
  }

  private static ItemStack waterBottle() {
    return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
  }

  public static boolean canFillBottle(ItemStack up, ItemStack down, FluidStorage tank) {
    return isEmptyBottle(up) && tank.getFluid() == Fluids.WATER && tank.getFluidAmount() >= BOTTLE_MB
        && outputAccepts(down, waterBottle());
  }

  private static boolean fillBottle(ItemStackHandler handler, int slotUp, int slotDown, FluidStorage tank) {
    ItemStack up = handler.getStackInSlot(slotUp);
    ItemStack down = handler.getStackInSlot(slotDown);
    if (!canFillBottle(up, down, tank)) {
      return false;
    }
    tank.drain(BOTTLE_MB, IFluidHandler.FluidAction.EXECUTE);
    ItemStack remaining = up.copy();
    remaining.shrink(1);
    handler.setStackInSlot(slotUp, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    if (down.isEmpty()) {
      handler.setStackInSlot(slotDown, waterBottle());
    } else {
      down.grow(1);
      handler.setStackInSlot(slotDown, down);
    }
    return true;
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

  private static int fillAmount(ItemStack stack) {
    return Math.min(1000, getCellCapacity(stack) - FluidItem.getFluidAmount(stack));
  }

  private static boolean fillsCell(ItemStack stack) {
    return FluidItem.getFluidAmount(stack) + fillAmount(stack) >= getCellCapacity(stack);
  }

  public static boolean hasEnoughToFill(ItemStack stack, FluidStorage tank) {
    if (tank.isEmpty() || !canReceiveFluid(stack, tank.getFluid())) {
      return false;
    }
    int amount = fillAmount(stack);
    if (amount <= 0 || tank.getFluidAmount() < amount) {
      return false;
    }
    return fillsCell(stack) || stack.getCount() <= 1;
  }

  private static boolean outputAccepts(ItemStack down, ItemStack result) {
    if (down.isEmpty()) {
      return true;
    }
    return down.getCount() + 1 <= down.getMaxStackSize() && ItemStack.isSameItemSameTags(down, result);
  }

  private static ItemStack filledCopy(ItemStack up, Fluid fluid) {
    ItemStack filled = up.copy();
    filled.setCount(1);
    FluidItem.setFluid(filled, fluid, FluidItem.getFluidAmount(up) + fillAmount(up));
    return filled;
  }

  private static ItemStack emptiedCopy(ItemStack up) {
    ItemStack emptied = up.copy();
    emptied.setCount(1);
    FluidItem.setFluid(emptied, Fluids.EMPTY, 0);
    return emptied;
  }

  public static boolean canDrainToCell(ItemStack up, ItemStack down, FluidStorage tank) {
    if (isEmptyBottle(up)) {
      return canFillBottle(up, down, tank);
    }
    if (isContainer(up)) {
      FluidActionResult result = FluidUtil.tryFillContainer(up, tank, Integer.MAX_VALUE, null, false);
      return result.isSuccess() && outputAccepts(down, result.getResult());
    }
    if (!hasEnoughToFill(up, tank)) {
      return false;
    }
    if (!down.isEmpty() && down.getCount() + 1 > down.getMaxStackSize()) {
      return false;
    }
    return !fillsCell(up) || outputAccepts(down, filledCopy(up, tank.getFluid()));
  }

  public static boolean drainToCell(ItemStackHandler handler, int slotUp, int slotDown, FluidStorage tank) {
    ItemStack up = handler.getStackInSlot(slotUp);
    ItemStack down = handler.getStackInSlot(slotDown);

    if (isEmptyBottle(up)) {
      return fillBottle(handler, slotUp, slotDown, tank);
    }
    if (isContainer(up)) {
      return moveContainer(handler, slotUp, slotDown, tank, true);
    }
    if (!canDrainToCell(up, down, tank)) {
      return false;
    }

    Fluid tankFluid = tank.getFluid();
    int amount = fillAmount(up);
    boolean full = fillsCell(up);
    ItemStack filled = filledCopy(up, tankFluid);
    tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);

    if (full) {
      ItemStack remaining = up.copy();
      remaining.shrink(1);
      handler.setStackInSlot(slotUp, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
      if (down.isEmpty()) {
        handler.setStackInSlot(slotDown, filled);
      } else {
        down.grow(1);
        handler.setStackInSlot(slotDown, down);
      }
    } else {
      handler.setStackInSlot(slotUp, filled);
    }
    return true;
  }

  public static boolean canFillFromCell(ItemStack up, ItemStack down, FluidStorage tank) {
    if (isContainer(up)) {
      FluidActionResult result = FluidUtil.tryEmptyContainer(up, tank, Integer.MAX_VALUE, null, false);
      return result.isSuccess() && outputAccepts(down, result.getResult());
    }
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

    if (tank.fill(new FluidStack(cellFluid, amount), IFluidHandler.FluidAction.SIMULATE) != amount) {
      return false;
    }
    return outputAccepts(down, emptiedCopy(up));
  }

  public static boolean fillFromCell(ItemStackHandler handler, int slotUp, int slotDown, FluidStorage tank) {
    ItemStack up = handler.getStackInSlot(slotUp);
    ItemStack down = handler.getStackInSlot(slotDown);

    if (isContainer(up)) {
      return moveContainer(handler, slotUp, slotDown, tank, false);
    }
    if (!canFillFromCell(up, down, tank)) {
      return false;
    }

    FluidStack cellStack = new FluidStack(FluidItem.getFluid(up), FluidItem.getFluidAmount(up));
    ItemStack emptied = emptiedCopy(up);
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
