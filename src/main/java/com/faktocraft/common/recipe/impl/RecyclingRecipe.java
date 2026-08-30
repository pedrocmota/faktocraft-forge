package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.faktocraft.common.registries.ModRecipeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

public class RecyclingRecipe implements IBaseRecipe<Container> {

  public static final RecipeSerializer<RecyclingRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final float chance;
  private final ItemStack result;
  private final List<Ingredient> excluded;

  public RecyclingRecipe(ResourceLocation id, float chance, ItemStack result, List<Ingredient> excluded) {
    this.id = id;
    this.chance = chance;
    this.result = result;
    this.excluded = List.copyOf(excluded);
  }

  @Override
  public boolean matches(Container container, Level level) {
    return !container.getItem(0).isEmpty() && !isExcluded(container.getItem(0));
  }

  public boolean isExcluded(ItemStack stack) {
    for (Ingredient ingredient : excluded) {
      if (ingredient.test(stack)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ItemStack assemble(Container container, RegistryAccess registryAccess) {
    return result.copy();
  }

  @Override
  public ItemStack getResultItem() {
    return result.copy();
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
  public ResourceLocation getId() {
    return id;
  }

  @Override
  public RecipeSerializer<RecyclingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<RecyclingRecipe> getType() {
    return ModRecipeType.RECYCLING;
  }

  public static class Serializer implements RecipeSerializer<RecyclingRecipe> {

    @Override
    public RecyclingRecipe fromJson(ResourceLocation id, JsonObject json) {
      float chance = GsonHelper.getAsFloat(json, "chance", 1.0F);
      ItemStack result = RecipeJsonHelper.result(json.get("result"));
      List<Ingredient> excluded = new ArrayList<>();
      if (json.has("excluded")) {
        JsonArray array = RecipeJsonHelper.array(json, "excluded");
        for (int i = 0; i < array.size(); i++) {
          excluded.add(RecipeJsonHelper.ingredient(array.get(i)));
        }
      }
      return new RecyclingRecipe(id, chance, result, excluded);
    }

    @Override
    public RecyclingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      float chance = buf.readFloat();
      ItemStack result = buf.readItem();
      int size = buf.readVarInt();
      List<Ingredient> excluded = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        excluded.add(Ingredient.fromNetwork(buf));
      }
      return new RecyclingRecipe(id, chance, result, excluded);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, RecyclingRecipe recipe) {
      buf.writeFloat(recipe.chance);
      buf.writeItem(recipe.result);
      buf.writeVarInt(recipe.excluded.size());
      for (Ingredient ingredient : recipe.excluded) {
        ingredient.toNetwork(buf);
      }
    }
  }
}
