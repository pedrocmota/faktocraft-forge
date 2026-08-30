package com.faktocraft.common.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record MachineRecipeInput(List<ItemStack> items) implements Container {

  public static MachineRecipeInput of(ItemStack... stacks) {
    return new MachineRecipeInput(List.of(stacks));
  }

  @Override
  public ItemStack getItem(int index) {
    return items.get(index);
  }

  public int size() {
    return items.size();
  }

  @Override
  public int getContainerSize() {
    return items.size();
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack stack : items) {
      if (!stack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    throw new UnsupportedOperationException("MachineRecipeInput is a read-only recipe input");
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    throw new UnsupportedOperationException("MachineRecipeInput is a read-only recipe input");
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    throw new UnsupportedOperationException("MachineRecipeInput is a read-only recipe input");
  }

  @Override
  public void setChanged() {
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public void clearContent() {
    throw new UnsupportedOperationException("MachineRecipeInput is a read-only recipe input");
  }
}
