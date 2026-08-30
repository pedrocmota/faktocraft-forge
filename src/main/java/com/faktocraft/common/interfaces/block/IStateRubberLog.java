package com.faktocraft.common.interfaces.block;

import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.world.level.block.state.BlockState;

public interface IStateRubberLog {

  default boolean isWet(BlockState state) {
    return state.getValue(BlockStateHelper.wetProperty);
  }

  default boolean isDry(BlockState state) {
    return state.getValue(BlockStateHelper.dryProperty);
  }

  default BlockState setWet(BlockState state, boolean wet) {
    return state.setValue(BlockStateHelper.wetProperty, wet);
  }

  default BlockState setDry(BlockState state, boolean dry) {
    return state.setValue(BlockStateHelper.dryProperty, dry);
  }
}
