package com.faktocraft.common.block.impl.cable;

import com.faktocraft.common.block.ISupportHost;
import com.faktocraft.common.energy.interfaces.IEnergyTransmitter;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.energy.provider.EnergyNetwork;
import com.faktocraft.common.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityCable extends BlockEntity implements IEnergyTransmitter, ISupportHost {

  public BlockEntityCable(BlockPos pos, BlockState state) {
    super(ModBlockEntities.CABLE, pos, state);
  }

  protected BlockEntityCable(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
      BlockState state) {
    super(type, pos, state);
  }

  @Nullable
  @Override
  public EnergyNetwork getNetwork() {
    if (level == null) {
      return null;
    }
    return EnergyCore.get(level).getNetworks().getNetwork(getBlockPos());
  }

  private net.minecraft.core.Direction supportDirection;

  private long supportCheckedAt = -SUPPORT_REFRESH_TICKS;

  @Override
  @org.jetbrains.annotations.Nullable
  public net.minecraft.core.Direction supportDirection() {
    if (level == null) {
      return null;
    }
    long now = level.getGameTime();
    if (now - supportCheckedAt >= SUPPORT_REFRESH_TICKS) {
      supportCheckedAt = now - Math.floorMod(worldPosition.hashCode(), SUPPORT_REFRESH_TICKS);
      supportDirection = com.faktocraft.common.block.PipeSupport.directionFor(level, worldPosition, getBlockState());
    }
    return supportDirection;
  }

}
