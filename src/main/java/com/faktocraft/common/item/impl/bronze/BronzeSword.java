package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;

public class BronzeSword extends SwordItem {

  public BronzeSword(Item.Properties properties) {
    super(ModTiers.BRONZE, 3, -2.4F, properties);
  }
}
