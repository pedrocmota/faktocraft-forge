package com.faktocraft.common.block;

import com.faktocraft.common.config.BasicConfig;
import com.faktocraft.common.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PipeSupport {

  private static final Direction[] ANY_SIDE = {
      Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

  private static final int JUNCTION_SPACING = 3;

  private static final int MIN_RUN_LENGTH = 3;

  private static final int MAX_RUN_WALK = 256;

  private PipeSupport() {
  }

  @Nullable
  public static Direction directionFor(BlockGetter level, BlockPos pos, BlockState state) {
    if (!BasicConfig.showPipeSupports()) {
      return null;
    }
    if (!(state.getBlock() instanceof VoxelBlock)) {
      return null;
    }

    if (isJunction(level, pos, state)) {
      Direction attach = pick(level, pos, ANY_SIDE);
      if (attach == null) {
        return null;
      }
      return junctionCrowded(level, pos, state) ? null : attach;
    }
    return gridDirection(level, pos, state);
  }

  private static boolean junctionCrowded(BlockGetter level, BlockPos pos, BlockState state) {
    for (Direction direction : Direction.values()) {
      if (!state.getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction))) {
        continue;
      }
      BlockPos cursor = pos;
      for (int step = 1; step <= JUNCTION_SPACING; step++) {
        cursor = cursor.relative(direction);
        BlockState other = level.getBlockState(cursor);
        if (!(other.getBlock() instanceof VoxelBlock)) {
          break;
        }
        if (isJunction(level, cursor, other)
            && pick(level, cursor, ANY_SIDE) != null
            && cursor.asLong() < pos.asLong()) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  private static Direction gridDirection(BlockGetter level, BlockPos pos, BlockState state) {
    Direction.Axis axis = runAxis(state, countConnections(state));
    if (axis == null) {
      return null;
    }
    Direction attach = pick(level, pos, sideOrder(axis));
    if (attach == null) {
      return null;
    }
    int spacing = Math.max(1, ModConfig.server().pipe_support_spacing);

    Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
    Direction positive = negative.getOpposite();
    BlockPos start = pos;
    int walked = 0;
    while (walked < MAX_RUN_WALK) {
      BlockPos prev = start.relative(negative);
      BlockState prevState = level.getBlockState(prev);
      if (!(prevState.getBlock() instanceof VoxelBlock)
          || runAxis(prevState, countConnections(prevState)) != axis) {
        break;
      }
      start = prev;
      walked++;
    }
    if (walked >= MAX_RUN_WALK) {
      return Math.floorMod(axis.choose(pos.getX(), pos.getY(), pos.getZ()), spacing) == 0
          ? attach : null;
    }

    int forward = 0;
    BlockPos ahead = pos;
    while (forward < MAX_RUN_WALK) {
      BlockPos next = ahead.relative(positive);
      BlockState nextState = level.getBlockState(next);
      if (!(nextState.getBlock() instanceof VoxelBlock)
          || runAxis(nextState, countConnections(nextState)) != axis) {
        break;
      }
      ahead = next;
      forward++;
    }
    if (walked + forward + 1 <= MIN_RUN_LENGTH) {
      return null;
    }

    int lastLeg = Integer.MIN_VALUE / 2;
    BlockPos before = start.relative(negative);
    BlockState beforeState = level.getBlockState(before);

    if (beforeState.getBlock() instanceof VoxelBlock && countConnections(beforeState) >= 2) {
      lastLeg = -1;
    }

    BlockPos cursor = start;
    for (int i = 0; i <= walked; i++) {
      boolean here = cursor.equals(pos);
      if (i - lastLeg >= spacing
          && (here || pick(level, cursor, sideOrder(axis)) != null)) {
        if (here) {
          return attach;
        }
        lastLeg = i;
      } else if (here) {
        return null;
      }
      cursor = cursor.relative(positive);
    }
    return null;
  }

  private static boolean isJunction(BlockGetter level, BlockPos pos, BlockState state) {
    return countConnections(state) >= 3 && countPipeNeighbours(level, pos, state) >= 3;
  }

  @Nullable
  private static Direction pick(BlockGetter level, BlockPos pos, Direction[] sides) {
    if (canAttach(level, pos, Direction.DOWN)) {
      return Direction.DOWN;
    }
    if (canAttach(level, pos, Direction.UP)) {
      return Direction.UP;
    }
    for (Direction side : sides) {
      if (canAttach(level, pos, side)) {
        return side;
      }
    }
    return null;
  }

  private static int countConnections(BlockState state) {
    int count = 0;
    for (Direction direction : Direction.values()) {
      if (state.getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction))) {
        count++;
      }
    }
    return count;
  }

  private static int countPipeNeighbours(BlockGetter level, BlockPos pos, BlockState state) {
    int count = 0;
    for (Direction direction : Direction.values()) {
      if (state.getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction))
          && level.getBlockState(pos.relative(direction)).getBlock() instanceof VoxelBlock) {
        count++;
      }
    }
    return count;
  }

  @Nullable
  private static Direction.Axis runAxis(BlockState state, int connections) {
    if (connections < 1 || connections > 2) {
      return null;
    }
    Direction.Axis found = null;
    for (Direction direction : Direction.values()) {
      if (state.getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction))) {
        if (found == null) {
          found = direction.getAxis();
        } else if (found != direction.getAxis()) {
          return null;
        }
      }
    }
    return found;
  }

  private static Direction[] sideOrder(Direction.Axis axis) {
    return switch (axis) {
      case X -> new Direction[] { Direction.NORTH, Direction.SOUTH };
      case Z -> new Direction[] { Direction.EAST, Direction.WEST };
      case Y -> ANY_SIDE;
    };
  }

  private static boolean canAttach(BlockGetter level, BlockPos pos, Direction direction) {
    BlockPos target = pos.relative(direction);
    BlockState state = level.getBlockState(target);
    if (state.isAir() || state.hasBlockEntity() || state.getBlock() instanceof VoxelBlock) {
      return false;
    }
    return state.isFaceSturdy(level, target, direction.getOpposite());
  }
}
