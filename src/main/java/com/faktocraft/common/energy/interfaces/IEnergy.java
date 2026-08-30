package com.faktocraft.common.energy.interfaces;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import java.util.Set;

public interface IEnergy {

  int energyStored();

  int maxEnergy();

  int setEnergy(int amount);

  void setMaxEnergy(int amount);

  boolean canReceiveEnergy(@Nullable Direction side);

  int maxReceiveTick();

  boolean canExtractEnergy(@Nullable Direction side);

  int maxExtractTick();

  EnergyType energyType();

  EnergyTier energyTier();

  void setEnergyTier(EnergyTier tier);

  default Set<EnergyTier> acceptedTiers() {
    return Set.of(energyTier());
  }

  default boolean acceptsTier(EnergyTier tier) {
    return acceptedTiers().contains(tier);
  }

  default EnergyTier maxAcceptedTier() {
    EnergyTier max = null;
    for (EnergyTier tier : acceptedTiers()) {
      if (max == null || tier.getLvl() > max.getLvl()) {
        max = tier;
      }
    }
    return max == null ? energyTier() : max;
  }

  default int maxReceive() {
    return Math.min(maxEnergy() - energyStored(), maxReceiveTick());
  }

  default int maxExtract() {
    return Math.min(energyStored(), maxExtractTick());
  }

  default int receiveEnergy(@Nullable Direction side, int amount, boolean simulate) {
    if (!canReceiveEnergy(side) && side != null) {
      return 0;
    }
    int received = Math.min(amount, maxReceive());
    if (!simulate) {
      setEnergy(energyStored() + received);
    }
    return received;
  }

  default int receiveEnergy(int amount, boolean simulate) {
    return receiveEnergy(null, amount, simulate);
  }

  default int extractEnergy(@Nullable Direction side, int amount, boolean simulate) {
    if (!canExtractEnergy(side) && side != null) {
      return 0;
    }
    int extracted = Math.min(energyStored(), Math.min(maxExtract(), amount));
    if (!simulate) {
      setEnergy(energyStored() - extracted);
    }
    return extracted;
  }

  default int extractEnergy(int amount, boolean simulate) {
    return extractEnergy(null, amount, simulate);
  }

  default int generateEnergy(int amount, boolean simulate) {
    if (maxExtractTick() == 0) {
      return 0;
    }
    int energy = Math.min(maxEnergy() - energyStored(), Math.min(maxEnergy(), amount));
    if (!simulate) {
      setEnergy(energyStored() + energy);
    }
    return energy;
  }

  default int consumeEnergy(int amount, boolean simulate) {
    int energy = Math.min(energyStored(), amount);
    if (!simulate) {
      setEnergy(energyStored() - energy);
    }
    return energy;
  }

  default void updated() {
  }
}
