package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.faktocraft.common.registries.ModRecipeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Optional;

public class ThermalCentrifugingRecipe implements IBaseRecipe<MachineRecipeInput> {
  public static final MapCodec<ThermalCentrifugingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      CountedIngredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
      RecipeJsonHelper.RESULT.optionalFieldOf("result_1").forGetter(recipe -> recipe.result1),
      RecipeJsonHelper.RESULT.optionalFieldOf("result_2").forGetter(recipe -> recipe.result2),
      Codec.INT.optionalFieldOf("temperature", 0).forGetter(recipe -> recipe.temperature),
      Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
      Codec.INT.optionalFieldOf("duration", 500).forGetter(recipe -> recipe.duration),
      Codec.INT.optionalFieldOf("power_cost", 48).forGetter(recipe -> recipe.powerCost))
      .apply(i, ThermalCentrifugingRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, ThermalCentrifugingRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        recipe.ingredient.toNetwork(buf);
        RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.encode(buf, recipe.result1);
        RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.encode(buf, recipe.result2);
        buf.writeVarInt(recipe.temperature);
        buf.writeFloat(recipe.experience);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.powerCost);
      }, buf -> {
        CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
        Optional<ItemStackTemplate> result1 = RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.decode(buf);
        Optional<ItemStackTemplate> result2 = RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.decode(buf);
        int temperature = buf.readVarInt();
        float experience = buf.readFloat();
        int duration = buf.readVarInt();
        int powerCost = buf.readVarInt();
        return new ThermalCentrifugingRecipe(ingredient, result1, result2, temperature, experience, duration,
            powerCost);
      });

  public static final RecipeSerializer<ThermalCentrifugingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final CountedIngredient ingredient;
  private final Optional<ItemStackTemplate> result1;
  private final Optional<ItemStackTemplate> result2;
  private final int temperature;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public ThermalCentrifugingRecipe(CountedIngredient ingredient, Optional<ItemStackTemplate> result1,
      Optional<ItemStackTemplate> result2, int temperature, float experience, int duration, int powerCost) {
    this.ingredient = ingredient;
    this.result1 = result1;
    this.result2 = result2;
    this.temperature = temperature;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(MachineRecipeInput input, Level level) {
    return ingredient.testType(input.getItem(0));
  }

  @Override
  public ItemStack assemble(MachineRecipeInput input) {
    return ItemStack.EMPTY;
  }

  public List<ItemStack> getResults() {
    return List.of(
        result1.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY),
        result2.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY));
  }

  public int getTemperature() {
    return temperature;
  }

  public Ingredient getIngredient() {
    return ingredient.ingredient();
  }

  public int getIngredientCount() {
    return ingredient.count();
  }

  @Override
  public float getExperience() {
    return experience;
  }

  @Override
  public int getDuration() {
    return duration;
  }

  @Override
  public int getPowerCost() {
    return powerCost;
  }

  @Override
  public RecipeSerializer<ThermalCentrifugingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<ThermalCentrifugingRecipe> getType() {
    return ModRecipeType.THERMAL_CENTRIFUGING;
  }
}
