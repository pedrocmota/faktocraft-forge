package com.faktocraft.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IPreRemoveSideEffects {
  void preRemoveSideEffects(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity);
}
