package com.faktocraft.common.recipe;

import com.faktocraft.common.interfaces.receipe.IChanceRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import java.util.List;
import java.util.Optional;

public abstract class BasicChanceRecipe extends BasicMachineRecipe implements IChanceRecipe {

  protected final RecipeChanceResult bonusResult;

  protected BasicChanceRecipe(ResourceLocation id, CountedIngredient ingredient, ItemStack result,
      Optional<ChanceResult> bonusResult,
      float experience, int duration, int powerCost) {
    super(id, ingredient, result, experience, duration, powerCost);
    this.bonusResult = RecipeChanceResult.of(bonusResult);
  }

  public RecipeChanceResult getBonusResult() {
    return bonusResult;
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
      return factory.create(id, ingredient, result, bonusResult, experience, duration, powerCost);
    }

    @Override
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      ItemStack result = buf.readItem();
      Optional<ChanceResult> bonusResult = buf.readOptional(ChanceResult::fromNetwork);
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return factory.create(id, ingredient, result, bonusResult, experience, duration, powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T recipe) {
      recipe.ingredient.toNetwork(buf);
      buf.writeItem(recipe.result);
      buf.writeOptional(recipe.bonusResult.firstResult(), (b, bonus) -> bonus.toNetwork(b));
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
