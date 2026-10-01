package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.BasicChanceRecipe;
import com.faktocraft.common.recipe.ChanceResult;
import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class ExtractingRecipe extends BasicChanceRecipe {

  public static final RecipeSerializer<ExtractingRecipe> SERIALIZER = BasicChanceRecipe.serializer(
      ExtractingRecipe::new);

  public ExtractingRecipe(CountedIngredient ingredient, @Nullable ItemStackTemplate result,
      Optional<ChanceResult> bonusResult, float experience, int duration, int powerCost) {
    super(ingredient, result, bonusResult, experience, duration, powerCost);
  }

  @Override
  public RecipeSerializer<ExtractingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<ExtractingRecipe> getType() {
    return ModRecipeType.EXTRACTING;
  }
}
