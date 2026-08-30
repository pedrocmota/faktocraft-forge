package com.faktocraft.common.energy.impl;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.registries.ModComponents;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemEnergyWrapper implements IEnergy {

  private final ItemStack stack;
  private final int maxEnergy;
  private final EnergyType energyType;
  private EnergyTier energyTier;

  public ItemEnergyWrapper(ItemStack stack, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    this.stack = stack;
    this.maxEnergy = maxEnergy;
    this.energyType = energyType;
    this.energyTier = energyTier;
  }

  @Override
  public int energyStored() {
    return Mth.clamp(ModComponents.getEnergy(stack, 0), 0, maxEnergy);
  }

  @Override
  public int maxEnergy() {
    return maxEnergy;
  }

  @Override
  public int setEnergy(int amount) {
    int clamped = Mth.clamp(amount, 0, maxEnergy);
    ModComponents.setEnergy(stack, clamped);
    updated();
    return clamped;
  }

  @Override
  public void setMaxEnergy(int amount) {
  }

  @Override
  public boolean canReceiveEnergy(@Nullable Direction side) {
    return energyType == EnergyType.RECEIVE || energyType == EnergyType.BOTH;
  }

  @Override
  public int maxReceiveTick() {
    return canReceiveEnergy(null) ? energyTier.getBasicTransfer() : 0;
  }

  @Override
  public boolean canExtractEnergy(@Nullable Direction side) {
    return energyType == EnergyType.EXTRACT || energyType == EnergyType.BOTH;
  }

  @Override
  public int maxExtractTick() {
    return canExtractEnergy(null) ? energyTier.getBasicTransfer() : 0;
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
    this.energyTier = tier;
  }
}
