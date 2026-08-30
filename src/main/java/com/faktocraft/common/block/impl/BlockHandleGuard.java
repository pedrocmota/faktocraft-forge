package com.faktocraft.common.block.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockHandleGuard extends Block {

  public static final DirectionProperty FACING = DirectionProperty.create("facing");

  private static final VoxelShape[] SHAPES = new VoxelShape[6];
  static {
    SHAPES[Direction.DOWN.get3DDataValue()] = Block.box(3, 0, 3, 13, 4, 13);
    SHAPES[Direction.UP.get3DDataValue()] = Block.box(3, 12, 3, 13, 16, 13);
    SHAPES[Direction.NORTH.get3DDataValue()] = Block.box(3, 3, 0, 13, 13, 4);
    SHAPES[Direction.SOUTH.get3DDataValue()] = Block.box(3, 3, 12, 13, 13, 16);
    SHAPES[Direction.WEST.get3DDataValue()] = Block.box(0, 3, 3, 4, 13, 13);
    SHAPES[Direction.EAST.get3DDataValue()] = Block.box(12, 3, 3, 16, 13, 13);
  }

  public BlockHandleGuard(Properties properties) {
    super(properties.strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
        .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return net.minecraft.world.phys.shapes.Shapes.empty();
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPES[state.getValue(FACING).get3DDataValue()];
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.INVISIBLE;
  }

  @Override
  public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
      boolean willHarvest, FluidState fluid) {
    return false;
  }

  private static final int VALIDATE_INTERVAL_TICKS = 100;

  @SuppressWarnings("deprecation")
  @Override
  public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);
    if (!level.isClientSide()) {
      level.scheduleTick(pos, this, VALIDATE_INTERVAL_TICKS);
    }
  }

  @Override
  public void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos,
      net.minecraft.util.RandomSource random) {
    if (!ownerValid(level, pos, state)) {
      level.removeBlock(pos, false);
      return;
    }
    level.scheduleTick(pos, this, VALIDATE_INTERVAL_TICKS);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos,
      boolean movedByPiston) {
    super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
    if (!level.isClientSide() && !ownerValid(level, pos, state)) {
      level.removeBlock(pos, false);
    }
  }

  private static boolean ownerValid(Level level, BlockPos pos, BlockState state) {
    Direction toOwner = state.getValue(FACING);
    BlockPos ownerPos = pos.relative(toOwner);
    BlockState ownerState = level.getBlockState(ownerPos);
    Direction outward = toOwner.getOpposite();
    if (ownerState.getBlock() instanceof com.faktocraft.common.block.impl.cable.BlockBreaker) {
      return ownerState.getValue(com.faktocraft.common.block.impl.cable.BlockBreaker.HANDLE) == outward;
    }
    return level.getBlockEntity(ownerPos) instanceof com.faktocraft.common.block.impl.pipe.IValveHolder holder
        && holder.getValve().isPresent() && holder.getValve().direction() == outward;
  }

  public static void place(Level level, BlockPos devicePos, Direction face) {
    if (level.isClientSide()) {
      return;
    }
    BlockPos target = devicePos.relative(face);
    if (level.getBlockState(target).isAir()) {
      level.setBlock(target, com.faktocraft.common.registries.ModBlocks.HANDLE_GUARD.defaultBlockState()
          .setValue(FACING, face.getOpposite()), 3);
    }
  }

  public static void remove(Level level, BlockPos devicePos, Direction face) {
    if (level.isClientSide()) {
      return;
    }
    BlockPos target = devicePos.relative(face);
    BlockState state = level.getBlockState(target);
    if (state.getBlock() instanceof BlockHandleGuard && state.getValue(FACING) == face.getOpposite()) {
      level.removeBlock(target, false);
    }
  }
}
