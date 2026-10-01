package com.faktocraft.common.block.impl.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockQuarryFrame extends Block {

  private static final Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;
  public static final BooleanProperty FOOT = BooleanProperty.create("foot");
  public static final EnumProperty<Connector> CONNECTOR = EnumProperty.create("connector", Connector.class);

  public enum Connector implements StringRepresentable {
    NONE(null),
    NORTH(Direction.NORTH),
    EAST(Direction.EAST),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    UP(Direction.UP),
    DOWN(Direction.DOWN);

    @Nullable
    private final Direction direction;

    Connector(@Nullable Direction direction) {
      this.direction = direction;
    }

    public static Connector of(Direction direction) {
      for (Connector connector : values()) {
        if (connector.direction == direction) {
          return connector;
        }
      }
      return NONE;
    }

    @Override
    public String getSerializedName() {
      return name().toLowerCase(java.util.Locale.ROOT);
    }
  }

  private static final VoxelShape CORE = Block.box(4, 4, 4, 12, 12, 12);
  private static final Map<Direction, VoxelShape> ARMS = new HashMap<>();

  static {
    ARMS.put(Direction.NORTH, Block.box(4, 4, 0, 12, 12, 4));
    ARMS.put(Direction.SOUTH, Block.box(4, 4, 12, 12, 12, 16));
    ARMS.put(Direction.WEST, Block.box(0, 4, 4, 4, 12, 12));
    ARMS.put(Direction.EAST, Block.box(12, 4, 4, 16, 12, 12));
    ARMS.put(Direction.DOWN, Block.box(4, 0, 4, 12, 4, 12));
    ARMS.put(Direction.UP, Block.box(4, 12, 4, 12, 16, 12));
  }

  private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

  public BlockQuarryFrame(Properties properties) {
    super(properties.strength(-1.0F, 3600000.0F).noLootTable()
        .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE));
    BlockState state = getStateDefinition().any();
    for (Direction direction : Direction.values()) {
      state = state.setValue(CONNECTIONS.get(direction), false);
    }
    registerDefaultState(state.setValue(FOOT, false).setValue(CONNECTOR, Connector.NONE));
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
    builder.add(FOOT);
    builder.add(CONNECTOR);
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
      net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack toolStack,
      boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
    return false;
  }

  public static BlockState connectedState(BlockState state, LevelReader level, BlockPos pos) {
    Connector connector = Connector.NONE;
    for (Direction direction : Direction.values()) {
      BlockState neighbor = level.getBlockState(pos.relative(direction));
      boolean linked = neighbor.is(state.getBlock()) || IGantryHost.isHost(neighbor);
      state = state.setValue(CONNECTIONS.get(direction), linked);
      if (connector == Connector.NONE && IGantryHost.isHost(neighbor)) {
        connector = Connector.of(direction);
      }
    }
    state = state.setValue(CONNECTOR, connector);
    return withFoot(state, level, pos, level.getBlockState(pos.below()));
  }

  private static BlockState withFoot(BlockState state, LevelReader level, BlockPos pos, BlockState ground) {
    return state.setValue(FOOT, hasGround(level, pos.below(), ground) && wantsFoot(state));
  }

  private static boolean hasGround(LevelReader level, BlockPos below, BlockState ground) {
    if (ground.isAir() || ground.hasBlockEntity() || ground.getBlock() instanceof BlockQuarryFrame) {
      return false;
    }
    return ground.isFaceSturdy(level, below, Direction.UP);
  }

  private static boolean wantsFoot(BlockState state) {
    Direction first = null;
    int count = 0;
    for (Direction direction : Direction.values()) {
      if (!state.getValue(CONNECTIONS.get(direction))) {
        continue;
      }
      count++;
      if (first == null) {
        first = direction;
      } else if (first.getAxis() != direction.getAxis()) {
        return true;
      }
    }
    return count != 2;
  }

  @Override
  public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
      Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
    boolean linked = neighborState.is(state.getBlock()) || IGantryHost.isHost(neighborState);
    state = state.setValue(CONNECTIONS.get(direction), linked);
    if (IGantryHost.isHost(neighborState)) {
      state = state.setValue(CONNECTOR, Connector.of(direction));
    } else if (state.getValue(CONNECTOR).direction == direction) {
      state = state.setValue(CONNECTOR, Connector.NONE);
    }
    BlockState ground = direction == Direction.DOWN ? neighborState : level.getBlockState(pos.below());
    return withFoot(state, level, pos, ground);
  }

  @Override
  public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
    return true;
  }
}
