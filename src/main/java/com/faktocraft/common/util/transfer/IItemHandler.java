package com.faktocraft.common.util.transfer;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IItemHandler {
  int getSlots();

  @NotNull
  ItemStack getStackInSlot(int slot);

  @NotNull
  ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

  @NotNull
  ItemStack extractItem(int slot, int amount, boolean simulate);

  int getSlotLimit(int slot);

  boolean isItemValid(int slot, @NotNull ItemStack stack);

  default void setStackInSlot(int slot, @NotNull ItemStack stack) {
    throw new UnsupportedOperationException("setStackInSlot not supported by " + getClass().getName());
  }

  default boolean supportsSetStackInSlot() {
    return false;
  }
}
