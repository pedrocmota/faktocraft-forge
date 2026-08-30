package com.faktocraft.common.tier;

import com.faktocraft.common.enums.EnergyTier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public enum CableTier {
  TIN_CABLE(EnergyTier.LOW, false),
  TIN_CABLE_INSULATED(EnergyTier.LOW, true),
  COPPER_CABLE(EnergyTier.MEDIUM, false),
  COPPER_CABLE_INSULATED(EnergyTier.MEDIUM, true),
  GOLD_CABLE(EnergyTier.HIGH, false),
  GOLD_CABLE_INSULATED(EnergyTier.HIGH, true),
  HV_CABLE(EnergyTier.VERY_HIGH, false),
  HV_CABLE_INSULATED(EnergyTier.VERY_HIGH, true),
  GLASS_FIBRE_CABLE(EnergyTier.ULTRA, true);

  private final EnergyTier energyTier;
  private final boolean insulated;

  CableTier(EnergyTier energyTier, boolean insulated) {
    this.energyTier = energyTier;
    this.insulated = insulated;
  }

  public EnergyTier getEnergyTier() {
    return energyTier;
  }

  public boolean isInsulated() {
    return insulated;
  }

  public BlockBehaviour.Properties createProperties() {
    if (this == GLASS_FIBRE_CABLE) {
      return BlockBehaviour.Properties.of().strength(0.8F, 0.8F).sound(SoundType.GLASS);
    }
    return BlockBehaviour.Properties.of().strength(0.8F, 0.8F).sound(SoundType.WOOL);
  }

  public static CableTier get(EnergyTier energyTier) {
    for (CableTier tier : values()) {
      if (tier.energyTier == energyTier) {
        return tier;
      }
    }
    return TIN_CABLE;
  }
}
