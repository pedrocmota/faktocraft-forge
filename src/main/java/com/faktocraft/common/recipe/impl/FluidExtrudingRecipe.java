package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.registries.ModRecipeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.faktocraft.common.util.RecipeUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class FluidExtrudingRecipe implements IBaseRecipe<MachineRecipeInput> {
  public static final MapCodec<FluidExtrudingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      CountedIngredient.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
      Codec.INT.optionalFieldOf("water_cost", 0).forGetter(recipe -> recipe.waterCost),
      Codec.INT.optionalFieldOf("lava_cost", 0).forGetter(recipe -> recipe.lavaCost),
      Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
      Codec.INT.optionalFieldOf("duration", 180).forGetter(recipe -> recipe.duration),
      Codec.INT.optionalFieldOf("power_cost", 8).forGetter(recipe -> recipe.powerCost))
      .apply(i, FluidExtrudingRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, FluidExtrudingRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        recipe.result.toNetwork(buf);
        buf.writeVarInt(recipe.waterCost);
        buf.writeVarInt(recipe.lavaCost);
        buf.writeFloat(recipe.experience);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.powerCost);
      }, buf -> {
        CountedIngredient result = CountedIngredient.fromNetwork(buf);
        int waterCost = buf.readVarInt();
        int lavaCost = buf.readVarInt();
        float experience = buf.readFloat();
        int duration = buf.readVarInt();
        int powerCost = buf.readVarInt();
        return new FluidExtrudingRecipe(result, waterCost, lavaCost, experience, duration, powerCost);
      });

  public static final RecipeSerializer<FluidExtrudingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final CountedIngredient result;
  private final int waterCost;
  private final int lavaCost;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public FluidExtrudingRecipe(CountedIngredient result, int waterCost, int lavaCost, float experience, int duration,
      int powerCost) {
    this.result = result;
    this.waterCost = waterCost;
    this.lavaCost = lavaCost;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(MachineRecipeInput input, Level level) {
    return result.testType(input.getItem(0));
  }

  @Override
  public ItemStack assemble(MachineRecipeInput input) {
    return getResultItem();
  }

  @Override
  public ItemStack getResultItem() {
    return RecipeUtil.ingredientItems(result.ingredient()).findFirst()
        .map(holder -> new ItemStack(holder.value()))
        .orElse(ItemStack.EMPTY);
  }

  public int getWaterCost() {
    return waterCost;
  }

  public int getLavaCost() {
    return lavaCost;
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
  public RecipeSerializer<FluidExtrudingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<FluidExtrudingRecipe> getType() {
    return ModRecipeType.FLUID_EXTRUDING;
  }
}
