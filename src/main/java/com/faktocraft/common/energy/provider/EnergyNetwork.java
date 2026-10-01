package com.faktocraft.common.energy.provider;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import java.util.Random;

public class EnergyNetwork implements IEnergy {

  private static final Random RANDOM = new Random();

  private int energy;
  private final HashSet<BlockPos> connections = new HashSet<>();
  private final HashSet<BlockPos> electrics = new HashSet<>();
  private final HashSet<BlockPos> transmitters = new HashSet<>();
  @Nullable
  private EnergyTier currentTier;

  @Nullable
  private EnergyTier lastCurrentTier;
  private EnergyTier energyTier = EnergyTier.LOW;

  public float r;
  public float g;
  public float b;

  public EnergyNetwork() {
    randomColor();
  }

  public EnergyNetwork(BlockPos pos, EnergyTier tier) {
    this.energyTier = tier;
    this.connections.add(pos);
    randomColor();
  }

  public EnergyNetwork(int energy, EnergyTier tier) {
    this.energy = energy;
    this.energyTier = tier;
    randomColor();
  }

  private void randomColor() {
    this.r = RANDOM.nextFloat();
    this.g = RANDOM.nextFloat();
    this.b = RANDOM.nextFloat();
  }

  private int lastReceived;
  private int lastDelivered;
  private int lastDemand;
  private int accReceived;
  private int accDelivered;
  private int accDemand;

  public void rollFlowStats() {
    lastReceived = accReceived;
    lastDelivered = accDelivered;
    lastDemand = accDemand;
    accReceived = 0;
    accDelivered = 0;
    accDemand = 0;
  }

  public void addFlowReceived(int amount) {
    accReceived += amount;
  }

  public void addFlowDelivered(int amount) {
    accDelivered += amount;
  }

  public void addFlowDemand(int amount) {
    accDemand += amount;
  }

  public int getLastReceived() {
    return lastReceived;
  }

  public int getLastDelivered() {
    return lastDelivered;
  }

  public int getLastDemand() {
    return lastDemand;
  }

  public HashSet<BlockPos> getConnections() {
    return connections;
  }

  public HashSet<BlockPos> getElectrics() {
    return electrics;
  }

  public HashSet<BlockPos> getTransmitters() {
    return transmitters;
  }

  public EnergyTier getEnergyTier() {
    return energyTier;
  }

  public void setCurrentTier(EnergyTier tier) {
    if (currentTier == null || currentTier.getLvl() < tier.getLvl()) {
      currentTier = tier;
    }
    lastCurrentTier = currentTier;
  }

  public EnergyTier getCurrentTier() {
    if (currentTier != null) {
      return currentTier;
    }
    if (lastCurrentTier != null && energy > 0) {
      return lastCurrentTier;
    }
    return EnergyTier.LOW;
  }

  @Nullable
  public EnergyTier getEnergyFlowing() {
    return currentTier;
  }

  public void resetCurrentTier() {
    currentTier = null;
  }

  @Override
  public int energyStored() {
    return energy;
  }

  @Override
  public int maxEnergy() {
    return energyTier.getBasicTransfer();
  }

  @Override
  public int setEnergy(int amount) {
    this.energy = Math.min(maxEnergy(), amount);
    return this.energy;
  }

  @Override
  public void setMaxEnergy(int amount) {
  }

  @Override
  public boolean canReceiveEnergy(@Nullable Direction side) {
    return true;
  }

  @Override
  public int maxReceiveTick() {
    return energyTier.getBasicTransfer();
  }

  @Override
  public boolean canExtractEnergy(@Nullable Direction side) {
    return true;
  }

  @Override
  public int maxExtractTick() {
    return energyTier.getBasicTransfer();
  }

  @Override
  public EnergyType energyType() {
    return EnergyType.CABLE;
  }

  @Override
  public EnergyTier energyTier() {
    return energyTier;
  }

  @Override
  public void setEnergyTier(EnergyTier tier) {
    this.energyTier = tier;
  }

  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    tag.putInt("energy", energy);
    tag.putInt("energyTier", energyTier.getLvl());
    tag.putLongArray("connections", connections.stream().mapToLong(BlockPos::asLong).toArray());
    tag.putLongArray("electrics", electrics.stream().mapToLong(BlockPos::asLong).toArray());
    tag.putLongArray("transmitters", transmitters.stream().mapToLong(BlockPos::asLong).toArray());
    if (lastCurrentTier != null) {
      tag.putInt("lastTier", lastCurrentTier.getLvl());
    }
    tag.putFloat("r", r);
    tag.putFloat("g", g);
    tag.putFloat("b", b);
    return tag;
  }

  public void deserializeNBT(CompoundTag tag) {
    this.energy = tag.contains("energy") ? tag.getIntOr("energy", 0) : 0;
    this.energyTier = EnergyTier.getTierFromLvl(tag.contains("energyTier") ? tag.getIntOr("energyTier", 0) : 1);
    connections.clear();
    for (long pos : tag.getLongArray("connections").orElse(new long[0])) {
      connections.add(BlockPos.of(pos));
    }
    electrics.clear();
    for (long pos : tag.getLongArray("electrics").orElse(new long[0])) {
      electrics.add(BlockPos.of(pos));
    }
    transmitters.clear();
    for (long pos : tag.getLongArray("transmitters").orElse(new long[0])) {
      transmitters.add(BlockPos.of(pos));
    }
    lastCurrentTier = tag.contains("lastTier") ? EnergyTier.getTierFromLvl(tag.getIntOr("lastTier", 0)) : null;
    this.r = tag.contains("r") ? tag.getFloatOr("r", 0.0F) : 0;
    this.g = tag.contains("g") ? tag.getFloatOr("g", 0.0F) : 0;
    this.b = tag.contains("b") ? tag.getFloatOr("b", 0.0F) : 0;
  }
}
