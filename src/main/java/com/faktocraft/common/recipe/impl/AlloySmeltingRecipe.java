package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IRecipeMultiInput;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlloySmeltingRecipe implements IRecipeMultiInput {
  public static final MapCodec<AlloySmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      CountedIngredient.CODEC.listOf(1, 3).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients),
      RecipeJsonHelper.RESULT.fieldOf("result").forGetter(recipe -> recipe.result),
      Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
      Codec.INT.optionalFieldOf("duration", 560).forGetter(recipe -> recipe.duration),
      Codec.INT.optionalFieldOf("power_cost", 32).forGetter(recipe -> recipe.powerCost))
      .apply(i, AlloySmeltingRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        buf.writeVarInt(recipe.ingredients.size());
        for (CountedIngredient ingredient : recipe.ingredients) {
          ingredient.toNetwork(buf);
        }
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
        buf.writeFloat(recipe.experience);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.powerCost);
      }, buf -> {
        int size = buf.readVarInt();
        List<CountedIngredient> ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
          ingredients.add(CountedIngredient.fromNetwork(buf));
        }
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
        float experience = buf.readFloat();
        int duration = buf.readVarInt();
        int powerCost = buf.readVarInt();
        return new AlloySmeltingRecipe(ingredients, result, experience, duration, powerCost);
      });

  public static final RecipeSerializer<AlloySmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,
      STREAM_CODEC);

  private final List<CountedIngredient> ingredients;
  private final ItemStackTemplate result;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public AlloySmeltingRecipe(List<CountedIngredient> ingredients, ItemStackTemplate result, float experience,
      int duration, int powerCost) {
    this.ingredients = List.copyOf(ingredients);
    this.result = result;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public Map<Ingredient, Integer> getIngredientMap() {
    Map<Ingredient, Integer> map = new LinkedHashMap<>();
    for (CountedIngredient countedIngredient : ingredients) {
      map.put(countedIngredient.ingredient(), countedIngredient.count());
    }
    return Collections.unmodifiableMap(map);
  }

  public int getIngredientCost(ItemStack stack) {
    for (CountedIngredient countedIngredient : ingredients) {
      if (countedIngredient.ingredient().test(stack)) {
        return countedIngredient.count();
      }
    }
    return 0;
  }

  @Override
  public boolean matches(MachineRecipeInput input, Level level) {
    if (input.size() == 1 && ingredients.size() > 1) {
      for (CountedIngredient countedIngredient : ingredients) {
        if (countedIngredient.ingredient().test(input.getItem(0))) {
          return true;
        }
      }
      return false;
    }
    return slotConsumption(input) != null;
  }

  @org.jetbrains.annotations.Nullable
  public int[] slotConsumption(MachineRecipeInput input) {
    int[] consume = new int[input.size()];
    if (!claim(input, 0, new boolean[input.size()], consume)) {
      return null;
    }
    for (int slot = 0; slot < input.size(); slot++) {
      if (consume[slot] == 0 && !input.getItem(slot).isEmpty()) {
        return null;
      }
    }
    return consume;
  }

  private boolean claim(MachineRecipeInput input, int index, boolean[] claimed, int[] consume) {
    if (index == ingredients.size()) {
      return true;
    }
    CountedIngredient countedIngredient = ingredients.get(index);
    for (int slot = 0; slot < input.size(); slot++) {
      if (claimed[slot] || !countedIngredient.test(input.getItem(slot))) {
        continue;
      }
      claimed[slot] = true;
      consume[slot] = countedIngredient.count();
      if (claim(input, index + 1, claimed, consume)) {
        return true;
      }
      claimed[slot] = false;
      consume[slot] = 0;
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
  public RecipeSerializer<AlloySmeltingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<AlloySmeltingRecipe> getType() {
    return ModRecipeType.ALLOY_SMELTING;
  }
}
