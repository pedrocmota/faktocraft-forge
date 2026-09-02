package com.faktocraft.common.block.impl.luminator;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityLuminator extends FaktocraftBlockEntity implements IEnergyBlock {

  public BlockEntityLuminator(BlockPos pos, BlockState state) {
    super(ModBlockEntities.LUMINATOR, pos, state);
    createEnergyStorage(0, ModConfig.server().luminator_energy_capacity, EnergyType.RECEIVE,
        EnergyTier.LOW, EnergyTier.MEDIUM);
  }

  @Override
  public boolean hasBatteryDock() {
    return false;
  }

  @Override
  public void tickWork(BlockState state) {
    int usage = Math.max(1, ModConfig.server().luminator_tick_usage);
    getEnergyStorage().updateConsumed(0);

    boolean active = getEnergyStorage().consumeEnergy(usage, true) >= usage;
    if (active) {
      getEnergyStorage().consumeEnergy(usage, false);
      getEnergyStorage().updateConsumed(usage);
    }

    this.setActive(active);
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }
}
