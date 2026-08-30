package com.faktocraft.common.item.impl;

import com.faktocraft.common.item.base.MaterialItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class Briquette extends MaterialItem {

  private static final int BURN_TIME_TICKS = 1600;

  public Briquette(Properties properties) {
    super(properties);
  }

  @Override
  public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
    return BURN_TIME_TICKS;
  }
}
