package com.faktocraft.common.item.base;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.world.item.Item;

public class EnergyStorageItem extends ElectricItem {

  public EnergyStorageItem(Item.Properties properties, int energyStored, int maxEnergy, EnergyType energyType,
      EnergyTier energyTier) {
    super(properties, energyStored, maxEnergy, energyType, energyTier);
  }
}
