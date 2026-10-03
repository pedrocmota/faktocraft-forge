package com.faktocraft.common.util.transfer;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InvWrapper implements IItemHandler {
  private final Container inv;

  public InvWrapper(Container inv) {
    this.inv = inv;
  }

  public Container getInv() {
    return inv;
  }

  @Override
  public int getSlots() {
    return inv.getContainerSize();
  }

  @NotNull
  @Override
  public ItemStack getStackInSlot(int slot) {
    return inv.getItem(slot);
  }

  @NotNull
  @Override
  public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack existing = inv.getItem(slot);
    int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    if (!existing.isEmpty()) {
      if (existing.getCount() >= Math.min(existing.getMaxStackSize(), getSlotLimit(slot))) {
        return stack;
      }
      if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
        return stack;
      }
      limit -= existing.getCount();
    }
    if (limit <= 0) {
      return stack;
    }
    if (!inv.canPlaceItem(slot, stack)) {
      return stack;
    }
    boolean reachedLimit = stack.getCount() > limit;
    if (!simulate) {
      int moved = reachedLimit ? limit : stack.getCount();
      inv.setItem(slot, stack.copyWithCount(existing.getCount() + moved));
      inv.setChanged();
    }
    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
  }

  @NotNull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack stackInSlot = inv.getItem(slot);
    if (stackInSlot.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (simulate) {
      if (stackInSlot.getCount() < amount) {
        return stackInSlot.copy();
      }
      return stackInSlot.copyWithCount(amount);
    }
    int m = Math.min(stackInSlot.getCount(), amount);
    ItemStack decr = inv.removeItem(slot, m);
    inv.setChanged();
    return decr;
  }

  @Override
  public int getSlotLimit(int slot) {
    return inv.getMaxStackSize();
  }

  @Override
  public boolean isItemValid(int slot, @NotNull ItemStack stack) {
    return inv.canPlaceItem(slot, stack);
  }

  @Override
  public void setStackInSlot(int slot, @NotNull ItemStack stack) {
    inv.setItem(slot, stack);
    inv.setChanged();
  }

  @Override
  public boolean supportsSetStackInSlot() {
    return true;
  }
}
