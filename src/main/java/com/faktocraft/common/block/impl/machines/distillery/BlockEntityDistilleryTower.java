package com.faktocraft.common.block.impl.machines.distillery;

import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.energy.interfaces.IEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockEntityDistilleryTower extends BlockEntity implements EnergyLookup.Provider {

  public BlockEntityDistilleryTower(BlockPos pos, BlockState state) {
    super(DistilleryRegistry.DISTILLERY_TOWER_BLOCK_ENTITY, pos, state);
  }

  @Nullable
  private BlockEntityDistillery getBase() {
    if (level == null) {
      return null;
    }
    for (int i = 1; i <= BlockDistillery.TOWER_HEIGHT + 1; i++) {
      if (level.getBlockEntity(worldPosition.below(i)) instanceof BlockEntityDistillery base) {
        return base;
      }
    }
    return null;
  }

  @Nullable
  @Override
  public IEnergy getEnergyLookup(@Nullable Direction side) {
    BlockEntityDistillery base = getBase();
    return base != null && base.hasEnergy() ? base.getEnergyStorage() : null;
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER || cap == ForgeCapabilities.ITEM_HANDLER) {
      BlockEntityDistillery base = getBase();
      if (base != null) {
        return base.getCapability(cap, side);
      }
    }
    return super.getCapability(cap, side);
  }
}
