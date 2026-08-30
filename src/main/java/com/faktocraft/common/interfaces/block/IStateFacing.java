package com.faktocraft.common.interfaces.block;

import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import java.util.Collection;

public interface IStateFacing {

  default Direction getDirection(BlockState state) {
    return state.getValue(getFacingProperty());
  }

  default BlockState setDirection(BlockState state, Direction direction) {
    if (supportsDirection(direction)) {
      return state.setValue(getFacingProperty(), direction);
    }
    return state;
  }

  default EnumProperty<Direction> getFacingProperty() {
    return BlockStateHelper.horizontalFacingProperty;
  }

  default Collection<Direction> getSupportedDirections() {
    return getFacingProperty().getPossibleValues();
  }

  default boolean supportsDirection(Direction direction) {
    return getSupportedDirections().contains(direction);
  }
}
