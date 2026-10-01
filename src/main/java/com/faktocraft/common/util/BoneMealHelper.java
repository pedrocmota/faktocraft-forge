package com.faktocraft.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BoneMealHelper {

  private BoneMealHelper() {
  }

  public static boolean grow(ItemStack stack, Level level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof BonemealableBlock target)
        || !target.isValidBonemealTarget(level, pos, state, BonemealSource.INTERACTION)) {
      return false;
    }
    if (level instanceof ServerLevel serverLevel) {
      if (target.isBonemealSuccess(serverLevel, serverLevel.getRandom(), pos, state, BonemealSource.INTERACTION)) {
        target.performBonemeal(serverLevel, serverLevel.getRandom(), pos, state, BonemealSource.INTERACTION);
      }
      stack.shrink(1);
    }
    return true;
  }
}
