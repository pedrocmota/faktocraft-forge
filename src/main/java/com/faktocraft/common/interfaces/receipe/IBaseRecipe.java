package com.faktocraft.common.interfaces.receipe;

import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

public interface IBaseRecipe<T extends RecipeInput> extends Recipe<T> {
  float getExperience();

  int getDuration();

  int getPowerCost();

  default ItemStack getResultItem() {
    return ItemStack.EMPTY;
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
  default String group() {
    return "";
  }

  @Override
  default PlacementInfo placementInfo() {
    return PlacementInfo.NOT_PLACEABLE;
  }

  @Override
  default RecipeBookCategory recipeBookCategory() {
    return ModRecipeType.MACHINE_BOOK_CATEGORY;
  }
}
