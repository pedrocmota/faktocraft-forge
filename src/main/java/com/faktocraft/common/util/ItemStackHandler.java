package com.faktocraft.common.util;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import com.faktocraft.common.util.transfer.IItemHandler;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemStackHandler implements net.minecraft.world.Container, IItemHandler {

  protected NonNullList<ItemStack> stacks;

  public ItemStackHandler() {
    this(1);
  }

  public ItemStackHandler(int size) {
    this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  @Override
  public int getContainerSize() {
    return stacks.size();
  }

  public int getSlots() {
    return stacks.size();
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack stack : stacks) {
      if (!stack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack getItem(int slot) {
    return stacks.get(slot);
  }

  public ItemStack getStackInSlot(int slot) {
    return stacks.get(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    ItemStack result = ContainerHelper.removeItem(stacks, slot, amount);
    if (!result.isEmpty()) {
      onContentsChanged(slot);
    }
    return result;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    return ContainerHelper.takeItem(stacks, slot);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    stacks.set(slot, stack);
    onContentsChanged(slot);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    setItem(slot, stack);
  }

  @Override
  public boolean supportsSetStackInSlot() {
    return true;
  }

  @Override
  public int getMaxStackSize() {
    return 64;
  }

  public int getSlotLimit(int slot) {
    return getMaxStackSize();
  }

  public boolean isItemValid(int slot, ItemStack stack) {
    return true;
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    return isItemValid(slot, stack);
  }

  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (!isItemValid(slot, stack)) {
      return stack;
    }

    ItemStack existing = stacks.get(slot);
    int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

    if (!existing.isEmpty()) {
      if (!ItemStack.isSameItemSameComponents(existing, stack)) {
        return stack;
      }
      limit -= existing.getCount();
    }
    if (limit <= 0) {
      return stack;
    }

    boolean reachedLimit = stack.getCount() > limit;
    if (!simulate) {
      if (existing.isEmpty()) {
        stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
      } else {
        existing.grow(reachedLimit ? limit : stack.getCount());
      }
      onContentsChanged(slot);
    }
    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
  }

  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack existing = stacks.get(slot);
    if (existing.isEmpty()) {
      return ItemStack.EMPTY;
    }

    int toExtract = Math.min(amount, existing.getMaxStackSize());
    if (existing.getCount() <= toExtract) {
      if (!simulate) {
        stacks.set(slot, ItemStack.EMPTY);
        onContentsChanged(slot);
        return existing;
      }
      return existing.copy();
    } else {
      if (!simulate) {
        stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
        onContentsChanged(slot);
      }
      return existing.copyWithCount(toExtract);
    }
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public void clearContent() {
    stacks.clear();
  }

  @Override
  public void setChanged() {
  }

  protected void onContentsChanged(int slot) {
  }

  public void save(CompoundTag tag) {
    NbtBridge.saveItems(tag, stacks);
  }

  public void load(CompoundTag tag) {
    stacks = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
    NbtBridge.loadItems(tag, stacks);
  }
}
