package com.faktocraft.common.util.transfer;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotItemHandler extends Slot {
  private static final SimpleContainer EMPTY = new SimpleContainer(0);
  private final IItemHandler itemHandler;
  private final int index;

  public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
    super(EMPTY, index, xPosition, yPosition);
    this.itemHandler = itemHandler;
    this.index = index;
  }

  @Override
  public boolean mayPlace(@NotNull ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    return itemHandler.isItemValid(index, stack);
  }

  @NotNull
  @Override
  public ItemStack getItem() {
    return itemHandler.getStackInSlot(index);
  }

  @Override
  public void set(@NotNull ItemStack stack) {
    itemHandler.setStackInSlot(index, stack);
    setChanged();
  }

  @Override
  public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {
  }

  @Override
  public int getMaxStackSize() {
    return itemHandler.getSlotLimit(index);
  }

  @Override
  public int getMaxStackSize(@NotNull ItemStack stack) {
    ItemStack maxAdd = stack.copy();
    int maxInput = stack.getMaxStackSize();
    maxAdd.setCount(maxInput);
    ItemStack currentStack = itemHandler.getStackInSlot(index);
    if (itemHandler.supportsSetStackInSlot()) {
      itemHandler.setStackInSlot(index, ItemStack.EMPTY);
      ItemStack remainder = itemHandler.insertItem(index, maxAdd, true);
      itemHandler.setStackInSlot(index, currentStack);
      return maxInput - remainder.getCount();
    }
    ItemStack remainder = itemHandler.insertItem(index, maxAdd, true);
    int current = currentStack.getCount();
    int added = maxInput - remainder.getCount();
    return current + added;
  }

  @Override
  public boolean mayPickup(Player playerIn) {
    return !itemHandler.extractItem(index, 1, true).isEmpty();
  }

  @NotNull
  @Override
  public ItemStack remove(int amount) {
    return itemHandler.extractItem(index, amount, false);
  }

  public IItemHandler getItemHandler() {
    return itemHandler;
  }
}
