package com.faktocraft.common.item.impl;

import com.faktocraft.common.item.base.BaseItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public class Scrap extends BaseItem {
  private static final int BURN_TIME_TICKS = 200;

  public Scrap(Properties properties) {
    super(properties.component(DataComponents.COOKING_FUEL,
        new CookingFuel(new ResolvableInt.Constant(BURN_TIME_TICKS), new ResolvableFloat.Constant(1.0F))));
  }
}
