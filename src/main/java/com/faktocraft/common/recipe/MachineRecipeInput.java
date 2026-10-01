package com.faktocraft.common.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import java.util.List;

public record MachineRecipeInput(List<ItemStack> items) implements RecipeInput {
  public static MachineRecipeInput of(ItemStack... stacks) {
    return new MachineRecipeInput(List.of(stacks));
  }

  @Override
  public ItemStack getItem(int index) {
    return items.get(index);
  }

  @Override
  public int size() {
    return items.size();
  }

  public int getContainerSize() {
    return items.size();
  }
}
