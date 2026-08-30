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

public class CrushingRecipe extends BasicChanceRecipe {

  public static final RecipeSerializer<CrushingRecipe> SERIALIZER = new BasicChanceRecipe.Serializer<>(
      CrushingRecipe::new);

  public CrushingRecipe(ResourceLocation id, CountedIngredient ingredient, ItemStack result,
      Optional<ChanceResult> bonusResult, float experience, int duration, int powerCost) {
    super(id, ingredient, result, bonusResult, experience, duration, powerCost);
  }

  @Override
  public RecipeSerializer<CrushingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<CrushingRecipe> getType() {
    return ModRecipeType.CRUSHING;
  }
}
