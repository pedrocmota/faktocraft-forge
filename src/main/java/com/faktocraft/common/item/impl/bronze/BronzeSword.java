package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;

public class BronzeSword extends BaseItem {

  public BronzeSword(Item.Properties properties) {
    super(properties.sword(ModTiers.BRONZE, 3, -2.4F));
  }
}
