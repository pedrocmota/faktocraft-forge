package com.faktocraft.common.recipe;

import com.faktocraft.common.interfaces.receipe.IChanceRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BasicChanceRecipe extends BasicMachineRecipe implements IChanceRecipe {

  protected RecipeChanceResult bonusResult;

  protected BasicChanceRecipe(ResourceLocation id, CountedIngredient ingredient, ItemStack result,
      Optional<ChanceResult> bonusResult,
      float experience, int duration, int powerCost) {
    super(id, ingredient, result, experience, duration, powerCost);
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
    T create(ResourceLocation id, CountedIngredient ingredient, ItemStack result, Optional<ChanceResult> bonusResult,
        float experience, int duration, int powerCost);
  }

  public static class Serializer<T extends BasicChanceRecipe> implements RecipeSerializer<T> {

    private final ChanceFactory<T> factory;

    public Serializer(ChanceFactory<T> factory) {
      this.factory = factory;
    }

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
      CountedIngredient ingredient = CountedIngredient.fromJson(json.get("ingredient"));
      ItemStack result = json.has("result")
          ? RecipeJsonHelper.result(json.get("result"))
          : ItemStack.EMPTY;
      Optional<ChanceResult> bonusResult = json.has("bonus_result")
          ? Optional.of(ChanceResult.fromJson(GsonHelper.getAsJsonObject(json, "bonus_result")))
          : Optional.empty();
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 180);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 8);
      T recipe = factory.create(id, ingredient, result, bonusResult, experience, duration, powerCost);
      if (json.has("bonus_results")) {
        List<ChanceResult> results = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "bonus_results")) {
          results.add(ChanceResult.fromJson(element.getAsJsonObject()));
        }
        recipe.setBonusResults(results);
      }
      return recipe;
    }

    @Override
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      ItemStack result = buf.readItem();
      Optional<ChanceResult> bonusResult = buf.readOptional(ChanceResult::fromNetwork);
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      T recipe = factory.create(id, ingredient, result, bonusResult, experience, duration, powerCost);
      int extra = buf.readVarInt();
      if (extra > 0) {
        List<ChanceResult> results = new ArrayList<>(extra);
        for (int i = 0; i < extra; i++) {
          results.add(ChanceResult.fromNetwork(buf));
        }
        recipe.setBonusResults(results);
      }
      return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T recipe) {
      recipe.ingredient.toNetwork(buf);
      buf.writeItem(recipe.result);
      buf.writeOptional(recipe.bonusResult.firstResult(), (b, bonus) -> bonus.toNetwork(b));
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
    }
  }
}
