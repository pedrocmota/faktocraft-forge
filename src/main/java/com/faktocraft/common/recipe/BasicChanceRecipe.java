package com.faktocraft.common.recipe;

import com.faktocraft.common.interfaces.receipe.IChanceRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BasicChanceRecipe extends BasicMachineRecipe implements IChanceRecipe {
  protected RecipeChanceResult bonusResult;

  protected BasicChanceRecipe(CountedIngredient ingredient, @Nullable ItemStackTemplate result,
      Optional<ChanceResult> bonusResult, float experience, int duration, int powerCost) {
    super(ingredient, result, experience, duration, powerCost);
    this.bonusResult = RecipeChanceResult.of(bonusResult);
  }

  public RecipeChanceResult getBonusResult() {
    return bonusResult;
  }

  protected void setBonusResults(List<ChanceResult> results) {
    this.bonusResult = RecipeChanceResult.of(results);
  }

  @Override
  public List<ChanceResult> getChanceResults() {
    return bonusResult.getResults();
  }

  @Override
  public ItemStack rollChanceResult(RandomSource random) {
    return bonusResult.rollResult(random);
  }

  @FunctionalInterface
  public interface ChanceFactory<T extends BasicChanceRecipe> {
    T create(CountedIngredient ingredient, @Nullable ItemStackTemplate result, Optional<ChanceResult> bonusResult,
        float experience, int duration, int powerCost);
  }

  public static <T extends BasicChanceRecipe> RecipeSerializer<T> serializer(ChanceFactory<T> factory) {
    return new RecipeSerializer<>(mapCodec(factory), streamCodec(factory));
  }

  private static <T extends BasicChanceRecipe> T create(ChanceFactory<T> factory, CountedIngredient ingredient,
      Optional<ItemStackTemplate> result, Optional<ChanceResult> bonusResult, List<ChanceResult> bonusResults,
      float experience, int duration, int powerCost) {
    T recipe = factory.create(ingredient, result.orElse(null), bonusResult, experience, duration, powerCost);
    if (!bonusResults.isEmpty()) {
      recipe.setBonusResults(bonusResults);
    }
    return recipe;
  }

  public static <T extends BasicChanceRecipe> MapCodec<T> mapCodec(ChanceFactory<T> factory) {
    return RecordCodecBuilder.mapCodec(i -> i.group(
        CountedIngredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
        RecipeJsonHelper.RESULT.optionalFieldOf("result").forGetter(recipe -> Optional.ofNullable(recipe.result)),
        ChanceResult.CODEC.optionalFieldOf("bonus_result")
            .forGetter(recipe -> recipe.bonusResult.getResults().size() == 1
                ? recipe.bonusResult.firstResult()
                : Optional.empty()),
        ChanceResult.CODEC.listOf().optionalFieldOf("bonus_results", List.of())
            .forGetter(recipe -> recipe.bonusResult.getResults().size() > 1
                ? recipe.bonusResult.getResults()
                : List.of()),
        Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
        Codec.INT.optionalFieldOf("duration", 180).forGetter(recipe -> recipe.duration),
        Codec.INT.optionalFieldOf("power_cost", 8).forGetter(recipe -> recipe.powerCost))
        .apply(i, (ingredient, result, bonusResult, bonusResults, experience, duration, powerCost) -> create(factory,
            ingredient, result, bonusResult, bonusResults, experience, duration, powerCost)));
  }

  public static <T extends BasicChanceRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(
      ChanceFactory<T> factory) {
    return StreamCodec.of((buf, recipe) -> {
      recipe.ingredient.toNetwork(buf);
      RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.encode(buf, Optional.ofNullable(recipe.result));
      buf.writeOptional(recipe.bonusResult.firstResult(), (b, bonus) -> bonus.toNetwork(buf));
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
      List<ChanceResult> results = recipe.bonusResult.getResults();
      buf.writeVarInt(results.size() > 1 ? results.size() : 0);
      if (results.size() > 1) {
        for (ChanceResult bonus : results) {
          bonus.toNetwork(buf);
        }
      }
    }, buf -> {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      ItemStackTemplate result = RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.decode(buf).orElse(null);
      Optional<ChanceResult> bonusResult = buf.readOptional(b -> ChanceResult.fromNetwork(buf));
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      T recipe = factory.create(ingredient, result, bonusResult, experience, duration, powerCost);
      int extra = buf.readVarInt();
      if (extra > 0) {
        List<ChanceResult> results = new ArrayList<>(extra);
        for (int i = 0; i < extra; i++) {
          results.add(ChanceResult.fromNetwork(buf));
        }
        recipe.setBonusResults(results);
      }
      return recipe;
    });
  }
}
