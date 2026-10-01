package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.BasicMachineRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CuttingRecipe extends BasicMachineRecipe {

  public static final RecipeSerializer<CuttingRecipe> SERIALIZER = BasicMachineRecipe.serializer(
      CuttingRecipe::new);

  public CuttingRecipe(CountedIngredient ingredient, ItemStackTemplate result, float experience, int duration,
      int powerCost) {
    super(ingredient, result, experience, duration, powerCost);
  }

  @Override
  public RecipeSerializer<CuttingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<CuttingRecipe> getType() {
    return ModRecipeType.CUTTING;
  }
}
