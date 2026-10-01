package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;

public class BronzePickaxe extends BaseItem {

  public BronzePickaxe(Item.Properties properties) {
    super(properties.pickaxe(ModTiers.BRONZE, 1, -2.8F));
  }
}
