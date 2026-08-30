package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

public class BronzeShovel extends ShovelItem {

  public BronzeShovel(Item.Properties properties) {
    super(ModTiers.BRONZE, 1.5F, -3.0F, properties);
  }
}
