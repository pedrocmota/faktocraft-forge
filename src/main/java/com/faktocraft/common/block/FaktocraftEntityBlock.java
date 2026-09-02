package com.faktocraft.common.block;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FaktocraftEntityBlock extends FaktocraftBlock implements EntityBlock {

  public FaktocraftEntityBlock(Properties properties) {
    super(properties);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return null;
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
      BlockEntityType<T> type) {
    if (level.isClientSide()) {
      return (lvl, pos, blockState, blockEntity) -> {
        if (blockEntity instanceof FaktocraftBlockEntity faktocraftBlockEntity) {
          faktocraftBlockEntity.tickClient(blockState);
        }
      };
    }
    return (lvl, pos, blockState, blockEntity) -> {
      if (blockEntity instanceof FaktocraftBlockEntity faktocraftBlockEntity) {
        faktocraftBlockEntity.tickServer(blockState);
      }
    };
  }

  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!state.is(newState.getBlock())) {
      if (level.getBlockEntity(pos) instanceof FaktocraftBlockEntity blockEntity) {
        blockEntity.preRemoveSideEffects(pos, state);
      }
    }
    super.onRemove(state, level, pos, newState, isMoving);
  }
}
