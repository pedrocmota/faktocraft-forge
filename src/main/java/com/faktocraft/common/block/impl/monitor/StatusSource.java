package com.faktocraft.common.block.impl.monitor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface StatusSource {

  void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
      CompoundTag out);

  void lines(BlockState state, CompoundTag data, List<StatusLine> out);
}
