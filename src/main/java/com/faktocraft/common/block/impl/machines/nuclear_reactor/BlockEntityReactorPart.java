package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import com.faktocraft.common.util.transfer.CapabilityBlockEntity;
import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.energy.interfaces.IEnergyProxy;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import com.faktocraft.common.util.transfer.Capability;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockEntityReactorPart extends CapabilityBlockEntity implements EnergyLookup.Provider, IEnergyProxy {

  public BlockEntityReactorPart(BlockPos pos, BlockState state) {
    super(NuclearReactorRegistry.REACTOR_PART_BLOCK_ENTITY, pos, state);
  }

  @Nullable
  public BlockEntityNuclearReactor core() {
    if (level == null) {
      return null;
    }
    BlockPos corePos = BlockNuclearReactor.corePos(getBlockState(), worldPosition);
    if (corePos != null && level.getBlockEntity(corePos) instanceof BlockEntityNuclearReactor reactor) {
      return reactor;
    }
    return null;
  }

  @Nullable
  @Override
  public IEnergy getEnergyLookup(@Nullable Direction side) {
    BlockEntityNuclearReactor reactor = core();
    return reactor == null ? null : reactor.getEnergyStorage();
  }

  @Nullable
  @Override
  public FaktocraftBlockEntity energyOwner() {
    return core();
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      BlockEntityNuclearReactor reactor = core();
      if (reactor != null) {
        return reactor.getCapability(cap, side);
      }
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide()) {
      EnergyCore.get(level).addEnergyBlock(worldPosition);
    }
  }
}
