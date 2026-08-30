package com.faktocraft.common.recipe;

import net.minecraftforge.registries.ForgeRegistries;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class RecipeJsonHelper {

  private RecipeJsonHelper() {
  }

  public record CountedIngredient(Ingredient ingredient, int count) {
  }

  public static Ingredient ingredient(JsonElement json) {
    return counted(json).ingredient();
  }

  public static CountedIngredient counted(JsonElement json) {
    if (json == null || json.isJsonNull()) {
      throw new JsonSyntaxException("Ingredient cannot be null");
    }
    if (json.isJsonPrimitive()) {
      String s = json.getAsString();
      if (s.startsWith("#")) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(s.substring(1)));
        return new CountedIngredient(Ingredient.of(tag), 1);
      }
      return new CountedIngredient(Ingredient.of(item(s)), 1);
    }
    JsonObject obj = json.getAsJsonObject();
    int count = GsonHelper.getAsInt(obj, "count", 1);
    if (obj.has("type")) {
      return new CountedIngredient(
          net.minecraftforge.common.crafting.CraftingHelper.getIngredient(obj, false), count);
    }
    if (obj.has("tag")) {
      TagKey<Item> tag = TagKey.create(Registries.ITEM,
          new ResourceLocation(GsonHelper.getAsString(obj, "tag")));
      return new CountedIngredient(Ingredient.of(tag), count);
    }
    if (obj.has("item")) {
      String s = GsonHelper.getAsString(obj, "item");
      if (s.startsWith("#")) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(s.substring(1)));
        return new CountedIngredient(Ingredient.of(tag), count);
      }
      return new CountedIngredient(Ingredient.of(item(s)), count);
    }
    throw new JsonSyntaxException("Ingredient must have 'item' or 'tag': " + obj);
  }

  public static ItemStack result(JsonElement json) {
    if (json.isJsonPrimitive()) {
      return new ItemStack(item(json.getAsString()));
    }
    JsonObject obj = json.getAsJsonObject();
    String id = obj.has("item") ? GsonHelper.getAsString(obj, "item") : GsonHelper.getAsString(obj, "id");
    int count = GsonHelper.getAsInt(obj, "count", 1);
    return new ItemStack(item(id), count);
  }

  public static FluidStack fluid(JsonObject obj) {
    ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(obj, "fluid"));
    if (!ForgeRegistries.FLUIDS.containsKey(id)) {
      throw new JsonSyntaxException("Unknown fluid: " + id);
    }
    Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
    int amount = GsonHelper.getAsInt(obj, "amount");
    return new FluidStack(fluid, amount);
  }

  public static Item item(String id) {
    ResourceLocation rl = new ResourceLocation(id);
    if (!ForgeRegistries.ITEMS.containsKey(rl)) {
      throw new JsonSyntaxException("Unknown item: " + id);
    }
    return ForgeRegistries.ITEMS.getValue(rl);
  }

  public static JsonArray array(JsonObject obj, String key) {
    return GsonHelper.getAsJsonArray(obj, key);
  }
}
