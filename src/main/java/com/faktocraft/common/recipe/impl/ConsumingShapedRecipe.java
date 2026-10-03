package com.faktocraft.common.recipe.impl;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class ConsumingShapedRecipe extends ShapedRecipe {
  public static final MapCodec<ConsumingShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
      CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
      ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
      ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result))
      .apply(i, ConsumingShapedRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, ConsumingShapedRecipe> STREAM_CODEC = StreamCodec
      .composite(
          Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
          CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
          ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
          ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
          ConsumingShapedRecipe::new);

  public static final RecipeSerializer<ConsumingShapedRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final ItemStackTemplate result;

  public ConsumingShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
      ShapedRecipePattern pattern, ItemStackTemplate result) {
    super(commonInfo, bookInfo, pattern, result);
    this.result = result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    return NonNullList.withSize(input.size(), ItemStack.EMPTY);
  }
}
