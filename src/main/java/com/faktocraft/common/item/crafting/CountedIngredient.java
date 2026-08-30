package com.faktocraft.common.item.crafting;

import com.google.gson.JsonElement;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public record CountedIngredient(Ingredient ingredient, int count) {

  public static CountedIngredient fromJson(JsonElement json) {
    RecipeJsonHelper.CountedIngredient counted = RecipeJsonHelper.counted(json);
    return new CountedIngredient(counted.ingredient(), counted.count());
  }

  public static CountedIngredient of(ItemLike item, int count) {
    return new CountedIngredient(Ingredient.of(item), count);
  }

  public void toNetwork(FriendlyByteBuf buf) {
    ingredient.toNetwork(buf);
    buf.writeVarInt(count);
  }

  public static CountedIngredient fromNetwork(FriendlyByteBuf buf) {
    Ingredient ingredient = Ingredient.fromNetwork(buf);
    return new CountedIngredient(ingredient, buf.readVarInt());
  }

  public boolean testType(ItemStack stack) {
    return ingredient.test(stack);
  }

  public boolean test(ItemStack stack) {
    return ingredient.test(stack) && stack.getCount() >= count;
  }
}
