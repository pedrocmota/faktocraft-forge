package com.faktocraft.common.block.impl.quarry;

import net.minecraft.world.level.block.state.BlockState;

public interface IGantryHost {

  static boolean isHost(BlockState state) {
    return state.getBlock() instanceof IGantryHost;
  }
}
