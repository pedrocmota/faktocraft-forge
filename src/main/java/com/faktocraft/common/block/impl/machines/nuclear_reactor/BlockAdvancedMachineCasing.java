package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import com.faktocraft.common.block.BlockResource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockAdvancedMachineCasing extends BlockResource {

  public BlockAdvancedMachineCasing(Properties properties) {
    super(properties);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);
    if (!isMoving && !oldState.is(this) && !level.isClientSide()) {
      level.scheduleTick(pos, this, 1);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    super.tick(state, level, pos, random);
    NuclearReactorMultiblock.tryForm(level, pos);
  }
}
