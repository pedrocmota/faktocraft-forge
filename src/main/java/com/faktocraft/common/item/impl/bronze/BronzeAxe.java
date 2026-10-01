package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;

public class BronzeAxe extends BaseItem {

  public BronzeAxe(Item.Properties properties) {
    super(properties.axe(ModTiers.BRONZE, 6.0F, -3.1F));
  }
}
