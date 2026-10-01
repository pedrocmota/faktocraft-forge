package com.faktocraft.common.item.crafting;

import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import java.util.Optional;

public record CountedIngredient(Ingredient ingredient, int count) {
  public static final Codec<CountedIngredient> CODEC = RecipeJsonHelper.COUNTED_INGREDIENT.xmap(
      counted -> new CountedIngredient(counted.ingredient(), counted.count()),
      counted -> new RecipeJsonHelper.CountedIngredient(counted.ingredient(), counted.count()));

  public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC = StreamCodec.composite(
      Ingredient.CONTENTS_STREAM_CODEC, CountedIngredient::ingredient,
      ByteBufCodecs.VAR_INT, CountedIngredient::count,
      CountedIngredient::new);

  public static final StreamCodec<RegistryFriendlyByteBuf, Optional<CountedIngredient>> OPTIONAL_STREAM_CODEC =
      ByteBufCodecs.optional(STREAM_CODEC);

  public static CountedIngredient fromJson(JsonElement json) {
    RecipeJsonHelper.CountedIngredient counted = RecipeJsonHelper.counted(json);
    return new CountedIngredient(counted.ingredient(), counted.count());
  }

  public static CountedIngredient of(ItemLike item, int count) {
    return new CountedIngredient(Ingredient.of(item), count);
  }

  public void toNetwork(RegistryFriendlyByteBuf buf) {
    STREAM_CODEC.encode(buf, this);
  }

  public static CountedIngredient fromNetwork(RegistryFriendlyByteBuf buf) {
    return STREAM_CODEC.decode(buf);
  }

  public boolean testType(ItemStack stack) {
    return ingredient.test(stack);
  }

  public boolean test(ItemStack stack) {
    return ingredient.test(stack) && stack.getCount() >= count;
  }
}
