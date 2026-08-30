package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.BasicChanceRecipe;
import com.faktocraft.common.recipe.ChanceResult;
import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.Optional;

public class SawingRecipe extends BasicChanceRecipe {

  public static final RecipeSerializer<SawingRecipe> SERIALIZER = new BasicChanceRecipe.Serializer<>(SawingRecipe::new);

  public SawingRecipe(ResourceLocation id, CountedIngredient ingredient, ItemStack result,
      Optional<ChanceResult> bonusResult, float experience, int duration, int powerCost) {
    super(id, ingredient, result, bonusResult, experience, duration, powerCost);
  }

  @Override
  public RecipeSerializer<SawingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<SawingRecipe> getType() {
    return ModRecipeType.SAWING;
  }
}
