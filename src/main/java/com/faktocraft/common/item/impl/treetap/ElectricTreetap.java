package com.faktocraft.common.item.impl.treetap;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.ElectricItem;

public class ElectricTreetap extends ElectricItem {

  public ElectricTreetap(Properties properties, int energyStored, int maxEnergy, EnergyType energyType,
      EnergyTier energyTier) {
    super(properties, energyStored, maxEnergy, energyType, energyTier);
  }
}
