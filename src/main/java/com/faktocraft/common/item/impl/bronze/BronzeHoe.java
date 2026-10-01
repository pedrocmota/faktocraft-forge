package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;

public class BronzeHoe extends BaseItem {

  public BronzeHoe(Item.Properties properties) {
    super(properties.hoe(ModTiers.BRONZE, -2, -1.0F));
  }
}
