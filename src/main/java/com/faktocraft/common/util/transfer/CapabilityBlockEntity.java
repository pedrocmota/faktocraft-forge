package com.faktocraft.common.util.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.faktocraft.common.util.LegacyNbtBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CapabilityBlockEntity extends LegacyNbtBlockEntity implements ICapabilityProvider {
  protected CapabilityBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    return LazyOptional.empty();
  }
}
