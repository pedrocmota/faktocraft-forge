package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.BasicMachineRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ExtrudingRecipe extends BasicMachineRecipe {

  public static final RecipeSerializer<ExtrudingRecipe> SERIALIZER = BasicMachineRecipe.serializer(
      ExtrudingRecipe::new);

  public ExtrudingRecipe(CountedIngredient ingredient, ItemStackTemplate result, float experience, int duration,
      int powerCost) {
    super(ingredient, result, experience, duration, powerCost);
  }

  @Override
  public RecipeSerializer<ExtrudingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<ExtrudingRecipe> getType() {
    return ModRecipeType.EXTRUDING;
  }
}
