package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;

public class BronzeShovel extends BaseItem {

  public BronzeShovel(Item.Properties properties) {
    super(properties.shovel(ModTiers.BRONZE, 1.5F, -3.0F));
  }
}
