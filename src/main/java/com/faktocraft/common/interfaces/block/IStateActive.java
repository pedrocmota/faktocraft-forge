package com.faktocraft.common.interfaces.block;

import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.world.level.block.state.BlockState;

public interface IStateActive {

  default boolean isActive(BlockState state) {
    return state.hasProperty(BlockStateHelper.activeProperty) && state.getValue(BlockStateHelper.activeProperty);
  }

  default BlockState setActive(BlockState state, boolean active) {
    if (state.hasProperty(BlockStateHelper.activeProperty)) {
      return state.setValue(BlockStateHelper.activeProperty, active);
    }
    return state;
  }
}
