package com.faktocraft.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BoneMealHelper {

  private BoneMealHelper() {
  }

  public static boolean grow(ItemStack stack, Level level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof BonemealableBlock target)
        || !target.isValidBonemealTarget(level, pos, state, level.isClientSide())) {
      return false;
    }
    if (level instanceof ServerLevel serverLevel) {
      if (target.isBonemealSuccess(serverLevel, serverLevel.getRandom(), pos, state)) {
        target.performBonemeal(serverLevel, serverLevel.getRandom(), pos, state);
      }
      stack.shrink(1);
    }
    return true;
  }
}
