package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.FluidIngredientData;
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

public class OreWashingRecipe implements IBaseRecipe<MachineRecipeInput> {
  public static final MapCodec<OreWashingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      CountedIngredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
      FluidIngredientData.CODEC.fieldOf("fluid_ingredient").forGetter(recipe -> recipe.fluidInput),
      FluidIngredientData.CODEC.optionalFieldOf("acid_ingredient").forGetter(recipe -> recipe.acidInput),
      RecipeJsonHelper.RESULT.optionalFieldOf("result_1").forGetter(recipe -> recipe.result1),
      RecipeJsonHelper.RESULT.optionalFieldOf("result_2").forGetter(recipe -> recipe.result2),
      Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
      Codec.INT.optionalFieldOf("duration", 500).forGetter(recipe -> recipe.duration),
      Codec.INT.optionalFieldOf("power_cost", 16).forGetter(recipe -> recipe.powerCost))
      .apply(i, OreWashingRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, OreWashingRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        recipe.ingredient.toNetwork(buf);
        recipe.fluidInput.toNetwork(buf);
        buf.writeBoolean(recipe.acidInput.isPresent());
        recipe.acidInput.ifPresent(acid -> acid.toNetwork(buf));
        RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.encode(buf, recipe.result1);
        RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.encode(buf, recipe.result2);
        buf.writeFloat(recipe.experience);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.powerCost);
      }, buf -> {
        CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
        FluidIngredientData fluidInput = FluidIngredientData.fromNetwork(buf);
        Optional<FluidIngredientData> acidInput = buf.readBoolean()
            ? Optional.of(FluidIngredientData.fromNetwork(buf))
            : Optional.empty();
        Optional<ItemStackTemplate> result1 = RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.decode(buf);
        Optional<ItemStackTemplate> result2 = RecipeJsonHelper.OPTIONAL_RESULT_STREAM_CODEC.decode(buf);
        float experience = buf.readFloat();
        int duration = buf.readVarInt();
        int powerCost = buf.readVarInt();
        return new OreWashingRecipe(ingredient, fluidInput, acidInput, result1, result2, experience, duration,
            powerCost);
      });

  public static final RecipeSerializer<OreWashingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final CountedIngredient ingredient;
  private final FluidIngredientData fluidInput;
  private final Optional<FluidIngredientData> acidInput;
  private final Optional<ItemStackTemplate> result1;
  private final Optional<ItemStackTemplate> result2;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public OreWashingRecipe(CountedIngredient ingredient, FluidIngredientData fluidInput,
      Optional<FluidIngredientData> acidInput, Optional<ItemStackTemplate> result1, Optional<ItemStackTemplate> result2,
      float experience, int duration, int powerCost) {
    this.ingredient = ingredient;
    this.fluidInput = fluidInput;
    this.acidInput = acidInput;
    this.result1 = result1;
    this.result2 = result2;
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

  public FluidIngredientData getFluidInput() {
    return fluidInput;
  }

  public Optional<FluidIngredientData> getAcidInput() {
    return acidInput;
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
  public RecipeSerializer<OreWashingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<OreWashingRecipe> getType() {
    return ModRecipeType.ORE_WASHING;
  }
}
