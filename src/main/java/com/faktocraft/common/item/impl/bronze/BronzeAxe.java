package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

public class BronzeAxe extends AxeItem {

  public BronzeAxe(Item.Properties properties) {
    super(ModTiers.BRONZE, 6.0F, -3.1F, properties);
  }
}
