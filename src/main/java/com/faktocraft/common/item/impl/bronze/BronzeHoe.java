package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

public class BronzeHoe extends HoeItem {

  public BronzeHoe(Item.Properties properties) {
    super(ModTiers.BRONZE, -2, -1.0F, properties);
  }
}
