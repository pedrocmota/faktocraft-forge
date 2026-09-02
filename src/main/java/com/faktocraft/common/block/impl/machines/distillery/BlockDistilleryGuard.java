package com.faktocraft.common.block.impl.machines.distillery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockDistilleryGuard extends Block {

  public BlockDistilleryGuard(Properties properties) {
    super(properties.strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
        .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.INVISIBLE;
  }

  @Override
  public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
    if (!isConduit(Block.byItem(context.getItemInHand().getItem()))) {
      return false;
    }
    BlockPos base = findBase(context.getLevel(), context.getClickedPos());
    return base != null && context.getClickedPos().getY() - base.getY() < BlockDistillery.TOWER_HEIGHT;
  }

  public static boolean isConduit(Block block) {
    return block instanceof com.faktocraft.common.block.impl.pipe.BlockFluidPipe
        || block instanceof com.faktocraft.common.block.impl.cable.BlockCable;
  }

  @org.jetbrains.annotations.Nullable
  static BlockPos findBase(net.minecraft.world.level.LevelReader level, BlockPos pos) {
    for (int dy = 0; dy <= BlockDistillery.TOWER_HEIGHT + 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (level.getBlockState(pos.offset(dx, -dy, dz)).getBlock() instanceof BlockDistillery) {
            return pos.offset(dx, -dy, dz);
          }
        }
      }
    }
    return null;
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return Shapes.empty();
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
      CollisionContext context) {
    return Shapes.empty();
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
