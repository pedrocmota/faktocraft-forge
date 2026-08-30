package com.faktocraft.common.block.impl.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.HashMap;
import java.util.Map;

public class BlockQuarryFrame extends Block {

  private static final Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;

  private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
  private static final Map<Direction, VoxelShape> ARMS = new HashMap<>();

  static {
    ARMS.put(Direction.NORTH, Block.box(5, 5, 0, 11, 11, 5));
    ARMS.put(Direction.SOUTH, Block.box(5, 5, 11, 11, 11, 16));
    ARMS.put(Direction.WEST, Block.box(0, 5, 5, 5, 11, 11));
    ARMS.put(Direction.EAST, Block.box(11, 5, 5, 16, 11, 11));
    ARMS.put(Direction.DOWN, Block.box(5, 0, 5, 11, 5, 11));
    ARMS.put(Direction.UP, Block.box(5, 11, 5, 11, 16, 11));
  }

  private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

  public BlockQuarryFrame(Properties properties) {
    super(properties.strength(-1.0F, 3600000.0F).noLootTable()
        .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    BlockState state = getStateDefinition().any();
    for (Direction direction : Direction.values()) {
      state = state.setValue(CONNECTIONS.get(direction), false);
    }
    registerDefaultState(state);
    for (BlockState possible : getStateDefinition().getPossibleStates()) {
      VoxelShape shape = CORE;
      for (Direction direction : Direction.values()) {
        if (possible.getValue(CONNECTIONS.get(direction))) {
          shape = Shapes.or(shape, ARMS.get(direction));
        }
      }
      shapes.put(possible, shape);
    }
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    for (Direction direction : Direction.values()) {
      builder.add(CONNECTIONS.get(direction));
    }
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return Shapes.empty();
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
      CollisionContext context) {
    return shapes.get(state);
  }

  @Override
  public boolean onDestroyedByPlayer(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
      net.minecraft.world.entity.player.Player player, boolean willHarvest,
      net.minecraft.world.level.material.FluidState fluid) {
    return false;
  }

  public static BlockState connectedState(BlockState state, LevelAccessor level, BlockPos pos) {
    for (Direction direction : Direction.values()) {
      BlockState neighbor = level.getBlockState(pos.relative(direction));
      boolean linked = neighbor.is(QuarryRegistry.QUARRY_FRAME) || neighbor.is(QuarryRegistry.QUARRY);
      state = state.setValue(CONNECTIONS.get(direction), linked);
    }
    return state;
  }

  @Override
  public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
    boolean linked = neighborState.is(QuarryRegistry.QUARRY_FRAME) || neighborState.is(QuarryRegistry.QUARRY);
    return state.setValue(CONNECTIONS.get(direction), linked);
  }
}
