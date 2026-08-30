package com.faktocraft.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockSheet extends Block {

  private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

  public BlockSheet(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties sheetProperties(float speedFactor) {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BROWN)
        .strength(0.5F)
        .speedFactor(speedFactor)
        .sound(SoundType.SLIME_BLOCK);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }
}
