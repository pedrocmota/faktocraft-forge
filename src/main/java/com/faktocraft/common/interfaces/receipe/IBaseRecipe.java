package com.faktocraft.common.interfaces.receipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public interface IBaseRecipe<T extends Container> extends Recipe<T> {

  float getExperience();

  int getDuration();

  int getPowerCost();

  default ItemStack getResultItem() {
    return ItemStack.EMPTY;
  }

  @Override
  default ItemStack getResultItem(RegistryAccess registryAccess) {
    return getResultItem();
  }

  @Override
  default boolean canCraftInDimensions(int width, int height) {
    return true;
  }

  @Override
  default boolean isSpecial() {
    return true;
  }

  @Override
  default boolean showNotification() {
    return false;
  }

  @Override
  default String getGroup() {
    return "";
  }
}
