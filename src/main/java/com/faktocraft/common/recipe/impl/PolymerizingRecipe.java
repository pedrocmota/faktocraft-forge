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

public class PolymerizingRecipe implements IBaseRecipe<MachineRecipeInput> {
  public static final MapCodec<PolymerizingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      CountedIngredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
      FluidIngredientData.CODEC.fieldOf("fluid_ingredient").forGetter(recipe -> recipe.fluidInput),
      RecipeJsonHelper.RESULT.fieldOf("result").forGetter(recipe -> recipe.result),
      Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
      Codec.INT.optionalFieldOf("duration", 300).forGetter(recipe -> recipe.duration),
      Codec.INT.optionalFieldOf("power_cost", 24).forGetter(recipe -> recipe.powerCost))
      .apply(i, PolymerizingRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, PolymerizingRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        recipe.ingredient.toNetwork(buf);
        recipe.fluidInput.toNetwork(buf);
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
        buf.writeFloat(recipe.experience);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.powerCost);
      }, buf -> {
        CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
        FluidIngredientData fluidInput = FluidIngredientData.fromNetwork(buf);
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
        float experience = buf.readFloat();
        int duration = buf.readVarInt();
        int powerCost = buf.readVarInt();
        return new PolymerizingRecipe(ingredient, fluidInput, result, experience, duration, powerCost);
      });

  public static final RecipeSerializer<PolymerizingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final CountedIngredient ingredient;
  private final FluidIngredientData fluidInput;
  private final ItemStackTemplate result;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public PolymerizingRecipe(CountedIngredient ingredient, FluidIngredientData fluidInput, ItemStackTemplate result,
      float experience, int duration, int powerCost) {
    this.ingredient = ingredient;
    this.fluidInput = fluidInput;
    this.result = result;
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
    return result.create();
  }

  public ItemStack getResult() {
    return result.create();
  }

  public FluidIngredientData getFluidInput() {
    return fluidInput;
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
  public RecipeSerializer<PolymerizingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<PolymerizingRecipe> getType() {
    return ModRecipeType.POLYMERIZING;
  }
}
