package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IRecipeMultiInput;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.faktocraft.common.registries.ModRecipeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CircuitAssemblingRecipe implements IRecipeMultiInput {

  public static final RecipeSerializer<CircuitAssemblingRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final List<CountedIngredient> ingredients;
  private final ItemStack result;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public CircuitAssemblingRecipe(ResourceLocation id, List<CountedIngredient> ingredients, ItemStack result,
      float experience, int duration, int powerCost) {
    this.id = id;
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
  public ItemStack assemble(MachineRecipeInput input, RegistryAccess registryAccess) {
    return result.copy();
  }

  @Override
  public ItemStack getResultItem() {
    return result.copy();
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
  public ResourceLocation getId() {
    return id;
  }

  @Override
  public RecipeSerializer<CircuitAssemblingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<CircuitAssemblingRecipe> getType() {
    return ModRecipeType.CIRCUIT_ASSEMBLING;
  }

  public static class Serializer implements RecipeSerializer<CircuitAssemblingRecipe> {

    @Override
    public CircuitAssemblingRecipe fromJson(ResourceLocation id, JsonObject json) {
      JsonArray array = RecipeJsonHelper.array(json, "ingredients");
      if (array.isEmpty() || array.size() > 3) {
        throw new JsonSyntaxException("'ingredients' must have between 1 and 3 entries, got " + array.size());
      }
      List<CountedIngredient> ingredients = new ArrayList<>(array.size());
      for (int i = 0; i < array.size(); i++) {
        ingredients.add(CountedIngredient.fromJson(array.get(i)));
      }
      ItemStack result = RecipeJsonHelper.result(json.get("result"));
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 200);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 10);
      return new CircuitAssemblingRecipe(id, ingredients, result, experience, duration, powerCost);
    }

    @Override
    public CircuitAssemblingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      int size = buf.readVarInt();
      List<CountedIngredient> ingredients = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        ingredients.add(CountedIngredient.fromNetwork(buf));
      }
      ItemStack result = buf.readItem();
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return new CircuitAssemblingRecipe(id, ingredients, result, experience, duration, powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, CircuitAssemblingRecipe recipe) {
      buf.writeVarInt(recipe.ingredients.size());
      for (CountedIngredient ingredient : recipe.ingredients) {
        ingredient.toNetwork(buf);
      }
      buf.writeItem(recipe.result);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
