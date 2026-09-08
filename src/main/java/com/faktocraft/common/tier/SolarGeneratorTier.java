package com.faktocraft.common.tier;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.enums.EnergyTier;

public enum SolarGeneratorTier {
  BASIC(EnergyTier.LOW),
  ADVANCED(EnergyTier.MEDIUM),
  HYBRID(EnergyTier.HIGH),
  QUANTUM(EnergyTier.VERY_HIGH),
  STELLAR(EnergyTier.ULTRA);

  private final EnergyTier energyTier;

  SolarGeneratorTier(EnergyTier energyTier) {
    this.energyTier = energyTier;
  }

  public EnergyTier getEnergyTier() {
    return energyTier;
  }

  public int getEnergyCapacity() {
    return switch (this) {
      case BASIC -> ModConfig.server().solar_generator_energy_capacity;
      case ADVANCED -> ModConfig.server().advanced_solar_generator_energy_capacity;
      case HYBRID -> ModConfig.server().hybrid_solar_generator_energy_capacity;
      case QUANTUM -> ModConfig.server().quantum_solar_generator_energy_capacity;
      case STELLAR -> ModConfig.server().stellar_solar_generator_energy_capacity;
    };
  }

  public int getDayGenerate() {
    return switch (this) {
      case BASIC -> ModConfig.server().solar_generator_day_tick_generate;
      case ADVANCED -> ModConfig.server().advanced_solar_generator_day_tick_generate;
      case HYBRID -> ModConfig.server().hybrid_solar_generator_day_tick_generate;
      case QUANTUM -> ModConfig.server().quantum_solar_generator_day_tick_generate;
      case STELLAR -> ModConfig.server().stellar_solar_generator_day_tick_generate;
    };
  }

  public int getNightGenerate() {
    return switch (this) {
      case BASIC -> ModConfig.server().solar_generator_night_tick_generate;
      case ADVANCED -> ModConfig.server().advanced_solar_generator_night_tick_generate;
      case HYBRID -> ModConfig.server().hybrid_solar_generator_night_tick_generate;
      case QUANTUM -> ModConfig.server().quantum_solar_generator_night_tick_generate;
      case STELLAR -> ModConfig.server().stellar_solar_generator_night_tick_generate;
    };
  }

  public int getMoonlightGenerate() {
    return switch (this) {
      case QUANTUM -> ModConfig.server().quantum_solar_generator_moonlight_tick_generate;
      case STELLAR -> ModConfig.server().stellar_solar_generator_moonlight_tick_generate;
      default -> 0;
    };
  }
}
