package com.faktocraft.common.energy;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class EnergyLookup {

  public interface Provider {
    @Nullable
    IEnergy getEnergyLookup(@Nullable Direction side);
  }

  private EnergyLookup() {
  }

  @Nullable
  public static IEnergy find(Level level, BlockPos pos, @Nullable Direction side) {
    BlockEntity be = level.getBlockEntity(pos);
    if (be == null) {
      return null;
    }
    if (be instanceof Provider provider) {
      IEnergy energy = provider.getEnergyLookup(side);
      if (energy != null) {
        return energy;
      }
    }
    if (be instanceof IndRebBlockEntity indRebBlockEntity && indRebBlockEntity.hasEnergy()) {
      return indRebBlockEntity.getEnergyStorage();
    }
    return null;
  }

  public static boolean isPresent(Level level, BlockPos pos, @Nullable Direction side) {
    return find(level, pos, side) != null;
  }
}
