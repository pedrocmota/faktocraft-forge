package com.faktocraft.common.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public final class IngredientUtil {
  private IngredientUtil() {
  }

  public static Ingredient ofTag(TagKey<Item> tag) {
    return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag));
  }
}
