package com.faktocraft.common.energy.impl;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.interfaces.entity.IProgress;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import java.util.Set;

public class BasicEnergyStorage implements IEnergy, IProgress {

  protected int energyStored;
  protected int maxEnergy;
  protected final EnergyType energyType;
  protected EnergyTier energyTier;
  protected EnumSet<EnergyTier> acceptedTiers;

  public final int origEnergy;
  public final EnergyTier origTier;
  public final EnumSet<EnergyTier> origAcceptedTiers;

  protected int lastGenerated;
  protected int totalGenerated;
  protected int lastConsumed;
  protected int totalConsumed;

  public BasicEnergyStorage(int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    this(energyStored, maxEnergy, energyType, EnumSet.of(energyTier));
  }

  public BasicEnergyStorage(int energyStored, int maxEnergy, EnergyType energyType, Set<EnergyTier> tiers) {
    if (tiers.isEmpty()) {
      throw new IllegalArgumentException("energy storage needs at least one tier");
    }
    this.energyStored = energyStored;
    this.maxEnergy = maxEnergy;
    this.energyType = energyType;
    this.acceptedTiers = EnumSet.copyOf(tiers);
    this.energyTier = highestOf(this.acceptedTiers);
    this.origEnergy = maxEnergy;
    this.origTier = this.energyTier;
    this.origAcceptedTiers = EnumSet.copyOf(this.acceptedTiers);
  }

  private static EnergyTier highestOf(Set<EnergyTier> tiers) {
    EnergyTier highest = null;
    for (EnergyTier tier : tiers) {
      if (highest == null || tier.getLvl() > highest.getLvl()) {
        highest = tier;
      }
    }
    return highest;
  }

  @Override
  public int energyStored() {
    return energyStored;
  }

  @Override
  public int maxEnergy() {
    return maxEnergy;
  }

  @Override
  public int setEnergy(int amount) {
    this.energyStored = Math.min(maxEnergy, amount);
    updated();
    return this.energyStored;
  }

  @Override
  public void setMaxEnergy(int amount) {
    this.maxEnergy = amount;
    if (energyStored > maxEnergy) {
      energyStored = amount;
    }
    updated();
  }

  @Override
  public boolean canReceiveEnergy(@Nullable Direction side) {
    return false;
  }

  @Override
  public int maxReceiveTick() {
    return energyType == EnergyType.RECEIVE || energyType == EnergyType.BOTH ? energyTier.getBasicTransfer() : 0;
  }

  @Override
  public boolean canExtractEnergy(@Nullable Direction side) {
    return false;
  }

  @Override
  public int maxExtractTick() {
    return energyType == EnergyType.EXTRACT || energyType == EnergyType.BOTH ? energyTier.getBasicTransfer() : 0;
  }

  @Override
  public EnergyType energyType() {
    return energyType;
  }

  @Override

  public EnergyTier energyTier() {
    return energyTier;
  }

  @Override
  public void setEnergyTier(EnergyTier tier) {
    setAcceptedTiers(EnumSet.of(tier));
  }

  @Override
  public Set<EnergyTier> acceptedTiers() {
    return acceptedTiers;
  }

  public void setAcceptedTiers(Set<EnergyTier> tiers) {
    if (tiers.isEmpty()) {
      return;
    }
    this.acceptedTiers = EnumSet.copyOf(tiers);
    this.energyTier = highestOf(this.acceptedTiers);
    updated();
  }

  public void applyTierShift(int levels) {
    EnumSet<EnergyTier> shifted = EnumSet.noneOf(EnergyTier.class);
    for (EnergyTier tier : origAcceptedTiers) {
      int base = tier.getLvl();
      shifted.add(base >= EnergyTier.VERY_HIGH.getLvl()
          ? tier
          : EnergyTier.getTierFromLvl(Math.min(EnergyTier.VERY_HIGH.getLvl(), base + levels)));
    }
    if (!shifted.equals(acceptedTiers)) {
      setAcceptedTiers(shifted);
    }
  }

  public void updateGenerated(int amount) {
    if (amount > 0) {
      lastGenerated = amount;
      totalGenerated += amount;
    }
  }

  public void updateConsumed(int amount) {
    if (amount > 0) {
      lastConsumed = amount;
      totalConsumed += amount;
    }
  }

  private boolean tickWanted;
  private boolean tickStalled;
  private int tickShortfall;
  private int windowTicks;
  private int windowShortfall;
  private boolean windowStalled;
  private int lastWindowShortfallPerTick;

  public void tickEnergyStats() {
    if (tickWanted && tickStalled) {
      windowShortfall += tickShortfall;
      windowStalled = true;
    }
    tickWanted = false;
    tickStalled = false;
    tickShortfall = 0;
    if (++windowTicks >= 20) {
      lastWindowShortfallPerTick = windowStalled ? Math.max(1, windowShortfall / 20) : 0;
      windowTicks = 0;
      windowShortfall = 0;
      windowStalled = false;
    }
  }

  @Override
  public int consumeEnergy(int amount, boolean simulate) {
    int energy = Math.min(energyStored(), amount);
    if (simulate) {
      tickWanted = true;
      tickStalled = energy < amount;
      tickShortfall = tickStalled ? amount - energy : 0;
    } else {
      setEnergy(energyStored() - energy);
    }
    return energy;
  }

  public int getDemandShortfall() {
    return lastWindowShortfallPerTick;
  }

  public int getLastGenerated() {
    return lastGenerated;
  }

  public int getTotalGenerated() {
    return totalGenerated;
  }

  public int getLastConsumed() {
    return lastConsumed;
  }

  public int getTotalConsumed() {
    return totalConsumed;
  }

  @Override
  public float getProgress() {
    return energyStored;
  }

  @Override
  public float getProgressMax() {
    return maxEnergy;
  }

  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    tag.putInt("energyStored", energyStored);
    tag.putInt("maxEnergy", maxEnergy);
    tag.putInt("lastGenerated", lastGenerated);
    tag.putInt("totalGenerated", totalGenerated);
    tag.putInt("lastConsumed", lastConsumed);
    tag.putInt("totalConsumed", totalConsumed);
    return tag;
  }

  public void deserializeNBT(CompoundTag tag) {
    this.energyStored = tag.contains("energyStored") ? tag.getInt("energyStored") : 0;
    this.maxEnergy = tag.contains("maxEnergy") ? tag.getInt("maxEnergy") : this.maxEnergy;
    this.lastGenerated = tag.contains("lastGenerated") ? tag.getInt("lastGenerated") : 0;
    this.totalGenerated = tag.contains("totalGenerated") ? tag.getInt("totalGenerated") : 0;
    this.lastConsumed = tag.contains("lastConsumed") ? tag.getInt("lastConsumed") : 0;
    this.totalConsumed = tag.contains("totalConsumed") ? tag.getInt("totalConsumed") : 0;
  }
}
