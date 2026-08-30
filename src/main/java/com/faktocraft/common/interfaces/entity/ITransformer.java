package com.faktocraft.common.interfaces.entity;

import com.faktocraft.common.enums.EnergyTier;

public interface ITransformer {
  EnergyTier energyExtractTier();

  EnergyTier energyReceiveTier();
}
