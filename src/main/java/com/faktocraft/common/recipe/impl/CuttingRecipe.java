package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.BasicMachineRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CuttingRecipe extends BasicMachineRecipe {

  public static final RecipeSerializer<CuttingRecipe> SERIALIZER = new BasicMachineRecipe.Serializer<>(
      CuttingRecipe::new);

  public CuttingRecipe(ResourceLocation id, CountedIngredient ingredient, ItemStack result, float experience,
      int duration, int powerCost) {
    super(id, ingredient, result, experience, duration, powerCost);
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
