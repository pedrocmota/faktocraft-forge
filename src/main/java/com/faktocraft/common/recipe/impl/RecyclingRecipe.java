package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.faktocraft.common.registries.ModRecipeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Optional;

public class RecyclingRecipe implements IBaseRecipe<MachineRecipeInput> {
  public static final MapCodec<RecyclingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(recipe -> recipe.chance),
      RecipeJsonHelper.RESULT.fieldOf("result").forGetter(recipe -> recipe.result),
      RecipeJsonHelper.INGREDIENT.listOf().optionalFieldOf("excluded", List.of()).forGetter(recipe -> recipe.excluded),
      RecipeJsonHelper.INGREDIENT.optionalFieldOf("ingredient").forGetter(recipe -> recipe.ingredient))
      .apply(i, RecyclingRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, RecyclingRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        buf.writeFloat(recipe.chance);
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.excluded);
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
      }, buf -> {
        float chance = buf.readFloat();
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
        List<Ingredient> excluded = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
        Optional<Ingredient> ingredient = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf);
        return new RecyclingRecipe(chance, result, excluded, ingredient);
      });

  public static final RecipeSerializer<RecyclingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final float chance;
  private final ItemStackTemplate result;
  private final List<Ingredient> excluded;
  private final Optional<Ingredient> ingredient;

  public RecyclingRecipe(float chance, ItemStackTemplate result, List<Ingredient> excluded) {
    this(chance, result, excluded, Optional.empty());
  }

  public RecyclingRecipe(float chance, ItemStackTemplate result, List<Ingredient> excluded,
      Optional<Ingredient> ingredient) {
    this.chance = chance;
    this.result = result;
    this.excluded = List.copyOf(excluded);
    this.ingredient = ingredient;
  }

  public boolean isSpecific() {
    return ingredient.isPresent();
  }

  public Optional<Ingredient> getIngredient() {
    return ingredient;
  }

  @Override
  public boolean matches(MachineRecipeInput input, Level level) {
    ItemStack stack = input.getItem(0);
    if (stack.isEmpty()) {
      return false;
    }
    return isSpecific() ? ingredient.get().test(stack) : !isExcluded(stack);
  }

  public boolean isExcluded(ItemStack stack) {
    for (Ingredient excludedIngredient : excluded) {
      if (excludedIngredient.test(stack)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ItemStack assemble(MachineRecipeInput input) {
    return result.create();
  }

  @Override
  public ItemStack getResultItem() {
    return result.create();
  }

  public float getChance() {
    return chance;
  }

  @Override
  public float getExperience() {
    return 0;
  }

  @Override
  public int getDuration() {
    return 45;
  }

  @Override
  public int getPowerCost() {
    return 1;
  }

  @Override
  public RecipeSerializer<RecyclingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<RecyclingRecipe> getType() {
    return ModRecipeType.RECYCLING;
  }
}
