package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.BasicMachineRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ExtrudingRecipe extends BasicMachineRecipe {

  public static final RecipeSerializer<ExtrudingRecipe> SERIALIZER = new BasicMachineRecipe.Serializer<>(
      ExtrudingRecipe::new);

  public ExtrudingRecipe(ResourceLocation id, CountedIngredient ingredient, ItemStack result, float experience,
      int duration, int powerCost) {
    super(id, ingredient, result, experience, duration, powerCost);
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
