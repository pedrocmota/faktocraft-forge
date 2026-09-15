package com.faktocraft.common.tier;

import com.faktocraft.common.enums.EnergyTier;

public enum TransformerTier {
  LOW(EnergyTier.LOW, EnergyTier.MEDIUM, true),
  MEDIUM(EnergyTier.MEDIUM, EnergyTier.HIGH, true),
  HIGH(EnergyTier.HIGH, EnergyTier.VERY_HIGH, true),
  VERY_HIGH(EnergyTier.VERY_HIGH, EnergyTier.ULTRA, false);

  private final EnergyTier minTier;
  private final EnergyTier maxTier;
  private final boolean stepUpAllowed;

  TransformerTier(EnergyTier minTier, EnergyTier maxTier, boolean stepUpAllowed) {
    this.minTier = minTier;
    this.maxTier = maxTier;
    this.stepUpAllowed = stepUpAllowed;
  }

  public boolean isStepUpAllowed() {
    return stepUpAllowed;
  }

  public EnergyTier getMinTier() {
    return minTier;
  }

  public EnergyTier getMaxTier() {
    return maxTier;
  }
}
