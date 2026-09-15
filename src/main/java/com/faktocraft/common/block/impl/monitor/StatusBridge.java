package com.faktocraft.common.block.impl.monitor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface StatusBridge {

  void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
      CompoundTag out);
}
