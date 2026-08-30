package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.block.VoxelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStonePipe extends VoxelBlock {

  public BlockStonePipe(Properties properties) {
    super(properties, 0.25f);
  }

  @Override
  protected boolean canConnect(LevelAccessor level, BlockPos pos, Direction direction) {
    var block = level.getBlockState(pos.relative(direction)).getBlock();
    return block instanceof BlockStonePipe || block instanceof BlockChassis
        || block instanceof BlockDockingPipe;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);
    if (!level.isClientSide()) {
      LogisticsCores.markDirtyNear(level, pos);
    }
  }

  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!state.is(newState.getBlock()) && !level.isClientSide()) {
      LogisticsCores.markDirtyNear(level, pos);
    }
    super.onRemove(state, level, pos, newState, isMoving);
  }
}
