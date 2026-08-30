package com.faktocraft.common.energy.interfaces;

import com.faktocraft.common.energy.provider.EnergyNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface IEnergyCore {
  void addEnergyBlock(BlockPos pos);

  void removeEnergyBlock(BlockPos pos);

  EnergyNetworks getNetworks();

  void tick();

  CompoundTag getNetworkTag(@Nullable BlockPos pos);

  void setNetworkTag(CompoundTag tag);
}
