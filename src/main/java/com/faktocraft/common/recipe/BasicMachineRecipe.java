package com.faktocraft.common.recipe;

import com.faktocraft.common.interfaces.receipe.IRecipeSingleIngredient;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class BasicMachineRecipe implements IRecipeSingleIngredient {
  protected final CountedIngredient ingredient;
  @Nullable
  protected final ItemStackTemplate result;
  protected final float experience;
  protected final int duration;
  protected final int powerCost;

  protected BasicMachineRecipe(CountedIngredient ingredient, @Nullable ItemStackTemplate result, float experience,
      int duration, int powerCost) {
    this.ingredient = ingredient;
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
    return getResultItem();
  }

  @Override
  public ItemStack getResultItem() {
    return result == null ? ItemStack.EMPTY : result.create();
  }

  public Ingredient getIngredient() {
    return ingredient.ingredient();
  }

  @Override
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

  @FunctionalInterface
  public interface Factory<T extends BasicMachineRecipe> {
    T create(CountedIngredient ingredient, ItemStackTemplate result, float experience, int duration, int powerCost);
  }

  public static <T extends BasicMachineRecipe> RecipeSerializer<T> serializer(Factory<T> factory) {
    return new RecipeSerializer<>(mapCodec(factory), streamCodec(factory));
  }

  public static <T extends BasicMachineRecipe> MapCodec<T> mapCodec(Factory<T> factory) {
    return RecordCodecBuilder.mapCodec(i -> i.group(
        CountedIngredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
        RecipeJsonHelper.RESULT.fieldOf("result").forGetter(recipe -> recipe.result),
        Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
        Codec.INT.optionalFieldOf("duration", 180).forGetter(recipe -> recipe.duration),
        Codec.INT.optionalFieldOf("power_cost", 8).forGetter(recipe -> recipe.powerCost))
        .apply(i, factory::create));
  }

  public static <T extends BasicMachineRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(
      Factory<T> factory) {
    return StreamCodec.of((buf, recipe) -> {
      recipe.ingredient.toNetwork(buf);
      ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }, buf -> {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return factory.create(ingredient, result, experience, duration, powerCost);
    });
  }
}
