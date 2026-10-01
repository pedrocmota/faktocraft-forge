package com.faktocraft.common.util;

import com.faktocraft.common.interfaces.block.IStateActive;
import com.faktocraft.common.interfaces.block.IStateAxis;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.interfaces.block.IStateRubberLog;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;

public class BlockStateHelper {

  public static final EnumProperty<Direction.Axis> axisProperty = BlockStateProperties.AXIS;
  public static final EnumProperty<Direction> facingProperty = BlockStateProperties.FACING;
  public static final EnumProperty<Direction> horizontalFacingProperty = BlockStateProperties.HORIZONTAL_FACING;
  public static final BooleanProperty activeProperty = BooleanProperty.create("active");
  public static final BooleanProperty wetProperty = BooleanProperty.create("wet");
  public static final BooleanProperty dryProperty = BooleanProperty.create("dry");
  public static final BooleanProperty waterlogged = BlockStateProperties.WATERLOGGED;

  public static BlockState getDefaultState(Block block, BlockState state) {
    if (block instanceof IStateActive) {
      state = state.setValue(activeProperty, false);
    }
    if (block instanceof IStateFacing facing) {
      state = state.setValue(facing.getFacingProperty(), Direction.NORTH);
    }
    if (block instanceof IStateAxis) {
      state = state.setValue(axisProperty, Direction.Axis.Y);
    }
    if (block instanceof IStateRubberLog) {
      state = state.setValue(wetProperty, false).setValue(dryProperty, false);
    }
    if (state.hasProperty(waterlogged)) {
      state = state.setValue(waterlogged, false);
    }
    return state;
  }

  public static BlockState getStateForPlacement(Block block, BlockState state, BlockPlaceContext context) {
    if (state == null) {
      return null;
    }
    if (block instanceof IStateFacing facing) {
      if (facing.getFacingProperty() == horizontalFacingProperty) {
        state = state.setValue(horizontalFacingProperty, context.getHorizontalDirection().getOpposite());
      } else {
        state = state.setValue(facing.getFacingProperty(), context.getNearestLookingDirection().getOpposite());
      }
    }
    if (block instanceof IStateAxis) {
      state = state.setValue(axisProperty, context.getClickedFace().getAxis());
    }
    if (state.hasProperty(waterlogged)) {
      state = state.setValue(waterlogged,
          context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }
    return state;
  }

  public static void fillBlockStateContainer(Block block, StateDefinition.Builder<Block, BlockState> builder) {
    if (block instanceof IStateActive) {
      builder.add(activeProperty);
    }
    if (block instanceof IStateFacing facing) {
      builder.add(facing.getFacingProperty());
    }
    if (block instanceof IStateAxis) {
      builder.add(axisProperty);
    }
    if (block instanceof IStateRubberLog) {
      builder.add(wetProperty, dryProperty);
    }
  }

  public static BlockState rotate(BlockState state, Rotation rotation) {
    if (state.getBlock() instanceof IStateFacing facing) {
      Direction direction = rotation.rotate(facing.getDirection(state));
      return facing.setDirection(state, direction);
    }
    return state;
  }

  public static BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
    return rotate(state, rotation);
  }

  @SuppressWarnings("deprecation")
  public static BlockState mirror(BlockState state, Mirror mirror) {
    if (state.getBlock() instanceof IStateFacing facing) {
      return state.rotate(mirror.getRotation(facing.getDirection(state)));
    }
    return state;
  }
}
