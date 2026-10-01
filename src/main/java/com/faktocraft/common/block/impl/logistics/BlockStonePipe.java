package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.block.VoxelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStonePipe extends VoxelBlock implements net.minecraft.world.level.block.EntityBlock {

  @org.jetbrains.annotations.Nullable
  @Override
  public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return com.faktocraft.common.cover.CoverSupport.isCovered(state)
        ? new com.faktocraft.common.cover.BlockEntityCoverHolder(pos, state)
        : null;
  }

  public BlockStonePipe(Properties properties) {
    super(properties, 0.25f);
  }

  @Override
  protected boolean canConnect(LevelReader level, BlockPos pos, Direction direction) {
    var block = level.getBlockState(pos.relative(direction)).getBlock();
    return block instanceof BlockStonePipe || block instanceof BlockChassis
        || block instanceof BlockDockingPipe;
  }

  @Override
  public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);
    if (!level.isClientSide()) {
      LogisticsCores.markDirtyNear(level, pos);
    }
  }

  @Override
  protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos,
      boolean movedByPiston) {
    LogisticsCores.markDirtyNear(level, pos);
    super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
  }
}
