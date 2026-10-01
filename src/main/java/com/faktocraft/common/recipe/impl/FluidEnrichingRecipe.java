package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.FluidIngredientData;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.registries.ModRecipeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class FluidEnrichingRecipe implements IBaseRecipe<MachineRecipeInput> {
  public static final MapCodec<FluidEnrichingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      CountedIngredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
      CountedIngredient.CODEC.optionalFieldOf("ingredient_2").forGetter(FluidEnrichingRecipe::getCountedIngredient2),
      FluidIngredientData.CODEC.fieldOf("fluid_ingredient").forGetter(recipe -> recipe.fluidInput),
      FluidIngredientData.CODEC.optionalFieldOf("fluid_ingredient_2").forGetter(FluidEnrichingRecipe::getFluidInput2),
      FluidIngredientData.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
      Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
      Codec.INT.optionalFieldOf("duration", 180).forGetter(recipe -> recipe.duration),
      Codec.INT.optionalFieldOf("power_cost", 8).forGetter(recipe -> recipe.powerCost))
      .apply(i, (ingredient, ingredient2, fluidInput, fluidInput2, result, experience, duration,
          powerCost) -> new FluidEnrichingRecipe(ingredient, ingredient2.orElse(null), fluidInput,
              fluidInput2.orElse(null), result, experience, duration, powerCost)));

  public static final StreamCodec<RegistryFriendlyByteBuf, FluidEnrichingRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        recipe.ingredient.toNetwork(buf);
        buf.writeBoolean(recipe.ingredient2 != null);
        if (recipe.ingredient2 != null) {
          recipe.ingredient2.toNetwork(buf);
        }
        recipe.fluidInput.toNetwork(buf);
        buf.writeBoolean(recipe.fluidInput2 != null);
        if (recipe.fluidInput2 != null) {
          recipe.fluidInput2.toNetwork(buf);
        }
        recipe.result.toNetwork(buf);
        buf.writeFloat(recipe.experience);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.powerCost);
      }, buf -> {
        CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
        CountedIngredient ingredient2 = buf.readBoolean() ? CountedIngredient.fromNetwork(buf) : null;
        FluidIngredientData fluidInput = FluidIngredientData.fromNetwork(buf);
        FluidIngredientData fluidInput2 = buf.readBoolean() ? FluidIngredientData.fromNetwork(buf) : null;
        FluidIngredientData result = FluidIngredientData.fromNetwork(buf);
        float experience = buf.readFloat();
        int duration = buf.readVarInt();
        int powerCost = buf.readVarInt();
        return new FluidEnrichingRecipe(ingredient, ingredient2, fluidInput, fluidInput2, result, experience,
            duration, powerCost);
      });

  public static final RecipeSerializer<FluidEnrichingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final CountedIngredient ingredient;
  @Nullable
  private final CountedIngredient ingredient2;
  private final FluidIngredientData fluidInput;
  @Nullable
  private final FluidIngredientData fluidInput2;
  private final FluidIngredientData result;
  private final float experience;
  private final int duration;
  private final int powerCost;
  @Nullable
  private PlacementInfo placementInfo;

  public FluidEnrichingRecipe(CountedIngredient ingredient, @Nullable CountedIngredient ingredient2,
      FluidIngredientData fluidInput, @Nullable FluidIngredientData fluidInput2, FluidIngredientData result,
      float experience, int duration, int powerCost) {
    this.ingredient = ingredient;
    this.ingredient2 = ingredient2;
    this.fluidInput = fluidInput;
    this.fluidInput2 = fluidInput2;
    this.result = result;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(MachineRecipeInput input, Level level) {
    ItemStack stack = input.getItem(0);
    return ingredient.testType(stack) || (ingredient2 != null && ingredient2.testType(stack));
  }

  @Override
  public ItemStack assemble(MachineRecipeInput input) {
    return ItemStack.EMPTY;
  }

  public FluidIngredientData getFluidInput() {
    return fluidInput;
  }

  public Optional<FluidIngredientData> getFluidInput2() {
    return Optional.ofNullable(fluidInput2);
  }

  public FluidIngredientData getResult() {
    return result;
  }

  public Ingredient getIngredient() {
    return ingredient.ingredient();
  }

  public CountedIngredient getCountedIngredient() {
    return ingredient;
  }

  public Optional<CountedIngredient> getCountedIngredient2() {
    return Optional.ofNullable(ingredient2);
  }

  public int getIngredientCount() {
    return ingredient.count();
  }

  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    list.add(ingredient.ingredient());
    if (ingredient2 != null) {
      list.add(ingredient2.ingredient());
    }
    return list;
  }

  @Override
  public PlacementInfo placementInfo() {
    if (placementInfo == null) {
      placementInfo = PlacementInfo.create(getIngredients());
    }
    return placementInfo;
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
  public RecipeSerializer<FluidEnrichingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<FluidEnrichingRecipe> getType() {
    return ModRecipeType.FLUID_ENRICHING;
  }
}
