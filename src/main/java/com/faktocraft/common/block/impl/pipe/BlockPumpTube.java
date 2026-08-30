package com.faktocraft.common.block.impl.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPumpTube extends Block {

  private static final VoxelShape SHAPE = Block.box(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);

  public BlockPumpTube(Properties properties) {
    super(properties.strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
        .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return net.minecraft.world.phys.shapes.Shapes.empty();
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.INVISIBLE;
  }

  @Override
  public boolean onDestroyedByPlayer(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
      net.minecraft.world.entity.player.Player player, boolean willHarvest,
      net.minecraft.world.level.material.FluidState fluid) {
    return false;
  }
}
