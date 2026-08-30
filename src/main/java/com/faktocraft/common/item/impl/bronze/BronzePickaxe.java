package com.faktocraft.common.item.impl.bronze;

import com.faktocraft.common.registries.ModTiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

public class BronzePickaxe extends PickaxeItem {

  public BronzePickaxe(Item.Properties properties) {
    super(ModTiers.BRONZE, 1, -2.8F, properties);
  }
}
