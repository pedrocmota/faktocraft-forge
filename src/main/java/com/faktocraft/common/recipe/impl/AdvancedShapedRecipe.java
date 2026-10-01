package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedRecipePattern;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import java.util.Optional;

public class AdvancedShapedRecipe extends ShapedRecipe {
  public static final MapCodec<AdvancedShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
      CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
      CountedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
      RecipeJsonHelper.RESULT.fieldOf("result").forGetter(recipe -> recipe.result))
      .apply(i, AdvancedShapedRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
      Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
      CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
      CountedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
      ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
      AdvancedShapedRecipe::new);

  public static final RecipeSerializer<AdvancedShapedRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final CountedRecipePattern pattern;
  private final ItemStackTemplate result;

  public AdvancedShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
      CountedRecipePattern pattern, ItemStackTemplate result) {
    super(commonInfo, bookInfo,
        new ShapedRecipePattern(pattern.width(), pattern.height(), pattern.plainIngredients(), Optional.empty()),
        result);
    this.pattern = pattern;
    this.result = result;
  }

  public AdvancedShapedRecipe(String group, CraftingBookCategory category, CountedRecipePattern pattern,
      ItemStack result, boolean showNotification) {
    this(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group), pattern,
        ItemStackTemplate.fromNonEmptyStack(result));
  }

  @Override
  @SuppressWarnings("unchecked")
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER;
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    return pattern.matches(input);
  }

  @Override
  public ItemStack assemble(CraftingInput input) {
    return result.create();
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    NonNullList<ItemStack> remainder = super.getRemainingItems(input);
    pattern.consumeExtra(input);
    return remainder;
  }

  public ItemStack getResultItem() {
    return result.create();
  }
}
