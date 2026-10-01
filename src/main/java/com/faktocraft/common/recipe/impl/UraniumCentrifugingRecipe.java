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

public class UraniumCentrifugingRecipe extends BasicChanceRecipe {

  public static final RecipeSerializer<UraniumCentrifugingRecipe> SERIALIZER = BasicChanceRecipe.serializer(
      UraniumCentrifugingRecipe::new);

  public UraniumCentrifugingRecipe(CountedIngredient ingredient, @Nullable ItemStackTemplate result,
      Optional<ChanceResult> bonusResult, float experience, int duration, int powerCost) {
    super(ingredient, result, bonusResult, experience, duration, powerCost);
  }

  @Override
  public RecipeSerializer<UraniumCentrifugingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<UraniumCentrifugingRecipe> getType() {
    return ModRecipeType.URANIUM_CENTRIFUGING;
  }
}
