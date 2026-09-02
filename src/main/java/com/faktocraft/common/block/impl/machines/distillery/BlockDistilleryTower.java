package com.faktocraft.common.block.impl.machines.distillery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockDistilleryTower extends Block implements net.minecraft.world.level.block.EntityBlock {

  public static final net.minecraft.world.level.block.state.properties.IntegerProperty SEGMENT =
      net.minecraft.world.level.block.state.properties.IntegerProperty
          .create("segment", 1,
              BlockDistillery.TOWER_HEIGHT + 1);

  private static final VoxelShape OUTLINE = Block.box(2.4, 0, 2.4, 13.6, 16, 13.6);
  private static final VoxelShape CHIMNEY = Block.box(3.8, 0, 3.8, 12.2, 13, 12.2);

  public BlockDistilleryTower(Properties properties) {
    super(properties.strength(3.0F, 10.0F).sound(SoundType.METAL).noLootTable().noOcclusion()
        .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    registerDefaultState(getStateDefinition().any().setValue(SEGMENT, 1));
    com.faktocraft.common.util.wrench.WrenchHelper.registerAction(this);
  }

  @Override
  protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(SEGMENT);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @org.jetbrains.annotations.Nullable
  @Override
  public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityDistilleryTower(pos, state);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return state.getValue(SEGMENT) > BlockDistillery.TOWER_HEIGHT ? CHIMNEY : OUTLINE;
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
      CollisionContext context) {
    return state.getValue(SEGMENT) > BlockDistillery.TOWER_HEIGHT ? CHIMNEY : Shapes.block();
  }

  @Nullable
  private BlockPos findBase(Level level, BlockPos pos) {
    for (int i = 1; i <= BlockDistillery.TOWER_HEIGHT + 1; i++) {
      if (level.getBlockState(pos.below(i)).getBlock() instanceof BlockDistillery) {
        return pos.below(i);
      }
    }
    return null;
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hit) {
    BlockPos basePos = findBase(level, pos);
    if (basePos != null) {
      return level.getBlockState(basePos).use(level, player, hand, hit.withPosition(basePos));
    }
    return InteractionResult.PASS;
  }

  @Override
  public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    BlockPos basePos = findBase(level, pos);
    if (basePos != null && !level.isClientSide()) {
      BlockState baseState = level.getBlockState(basePos);
      if (!player.isCreative()
          && player.getMainHandItem().is(com.faktocraft.common.registries.ModTags.WRENCHES)) {
        com.faktocraft.common.util.wrench.WrenchHelper.dismantleBlock(baseState, level, basePos);
      } else {
        boolean drop = !player.isCreative() && player.hasCorrectToolForDrops(baseState);
        level.destroyBlock(basePos, drop, player);
      }
    }
    super.playerWillDestroy(level, pos, state, player);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos,
      boolean movedByPiston) {
    super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
    if (level.isClientSide()) {
      return;
    }
    if (findBase(level, pos) == null) {
      level.removeBlock(pos, false);
    }
  }

  @Override
  public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
    return true;
  }
}
