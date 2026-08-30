package com.faktocraft.common.tier;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.enums.EnergyTier;

public enum BatteryBoxTier {
  BASIC(EnergyTier.LOW),
  STANDARD(EnergyTier.MEDIUM),
  ADVANCED(EnergyTier.HIGH),
  SUPER(EnergyTier.VERY_HIGH);

  private final EnergyTier energyTier;

  BatteryBoxTier(EnergyTier energyTier) {
    this.energyTier = energyTier;
  }

  public EnergyTier getEnergyTier() {
    return energyTier;
  }

  public int getEnergyCapacity() {
    return switch (this) {
      case BASIC -> ModConfig.server().wooden_battery_box_capacity;
      case STANDARD -> ModConfig.server().cesu_capacity;
      case ADVANCED -> ModConfig.server().mfe_capacity;
      case SUPER -> ModConfig.server().mfsu_capacity;
    };
  }
}
