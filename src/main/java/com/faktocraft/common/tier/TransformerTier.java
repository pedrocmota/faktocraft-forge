package com.faktocraft.common.tier;

import com.faktocraft.common.enums.EnergyTier;

public enum TransformerTier {
  LOW(EnergyTier.LOW, EnergyTier.MEDIUM),
  MEDIUM(EnergyTier.MEDIUM, EnergyTier.HIGH),
  HIGH(EnergyTier.HIGH, EnergyTier.VERY_HIGH),
  VERY_HIGH(EnergyTier.VERY_HIGH, EnergyTier.ULTRA);

  private final EnergyTier minTier;
  private final EnergyTier maxTier;

  TransformerTier(EnergyTier minTier, EnergyTier maxTier) {
    this.minTier = minTier;
    this.maxTier = maxTier;
  }

  public EnergyTier getMinTier() {
    return minTier;
  }

  public EnergyTier getMaxTier() {
    return maxTier;
  }
}
