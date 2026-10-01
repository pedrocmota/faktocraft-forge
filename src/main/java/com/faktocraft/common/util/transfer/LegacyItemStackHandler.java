package com.faktocraft.common.util.transfer;

import com.faktocraft.common.util.NbtBridge;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LegacyItemStackHandler implements IItemHandler {
  protected NonNullList<ItemStack> stacks;

  public LegacyItemStackHandler() {
    this(1);
  }

  public LegacyItemStackHandler(int size) {
    stacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  public LegacyItemStackHandler(NonNullList<ItemStack> stacks) {
    this.stacks = stacks;
  }

  public void setSize(int size) {
    stacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  @Override
  public void setStackInSlot(int slot, @NotNull ItemStack stack) {
    validateSlotIndex(slot);
    stacks.set(slot, stack);
    onContentsChanged(slot);
  }

  @Override
  public boolean supportsSetStackInSlot() {
    return true;
  }

  @Override
  public int getSlots() {
    return stacks.size();
  }

  @NotNull
  @Override
  public ItemStack getStackInSlot(int slot) {
    validateSlotIndex(slot);
    return stacks.get(slot);
  }

  @NotNull
  @Override
  public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (!isItemValid(slot, stack)) {
      return stack;
    }
    validateSlotIndex(slot);
    ItemStack existing = stacks.get(slot);
    int limit = getStackLimit(slot, stack);
    if (!existing.isEmpty()) {
      if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
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

  @NotNull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    validateSlotIndex(slot);
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
    }
    if (!simulate) {
      stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
      onContentsChanged(slot);
    }
    return existing.copyWithCount(toExtract);
  }

  @Override
  public int getSlotLimit(int slot) {
    return 64;
  }

  protected int getStackLimit(int slot, @NotNull ItemStack stack) {
    return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
  }

  @Override
  public boolean isItemValid(int slot, @NotNull ItemStack stack) {
    return true;
  }

  public CompoundTag serializeNBT() {
    ListTag list = new ListTag();
    for (int i = 0; i < stacks.size(); i++) {
      if (!stacks.get(i).isEmpty()) {
        CompoundTag itemTag = NbtBridge.saveStack(stacks.get(i));
        itemTag.putInt("Slot", i);
        list.add(itemTag);
      }
    }
    CompoundTag tag = new CompoundTag();
    tag.put("Items", list);
    tag.putInt("Size", stacks.size());
    return tag;
  }

  public void deserializeNBT(CompoundTag tag) {
    setSize(tag.contains("Size") ? tag.getIntOr("Size", stacks.size()) : stacks.size());
    ListTag list = tag.getListOrEmpty("Items");
    for (int i = 0; i < list.size(); i++) {
      CompoundTag itemTag = list.getCompoundOrEmpty(i);
      int slot = itemTag.getIntOr("Slot", -1);
      if (slot >= 0 && slot < stacks.size()) {
        stacks.set(slot, NbtBridge.loadStack(itemTag));
      }
    }
    onLoad();
  }

  protected void validateSlotIndex(int slot) {
    if (slot < 0 || slot >= stacks.size()) {
      throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }
  }

  protected void onLoad() {
  }

  protected void onContentsChanged(int slot) {
  }
}
