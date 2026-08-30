package com.faktocraft.common.interfaces.block;

import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IStateAxis {

  default Direction.Axis getAxis(BlockState state) {
    return state.getValue(BlockStateHelper.axisProperty);
  }
}
