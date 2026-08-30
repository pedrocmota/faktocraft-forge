package com.faktocraft.common.item.impl;

import com.faktocraft.common.item.base.BaseItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class Scrap extends BaseItem {

  public Scrap(Properties properties) {
    super(properties);
  }

  @Override
  public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
    return 200;
  }
}
