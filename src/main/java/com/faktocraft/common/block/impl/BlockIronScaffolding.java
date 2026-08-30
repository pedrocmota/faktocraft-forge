package com.faktocraft.common.block.impl;

import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockIronScaffolding extends ScaffoldingBlock {

  public BlockIronScaffolding(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties scaffoldingProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.METAL)
        .strength(3F, 5F)
        .sound(SoundType.METAL)
        .noCollission()
        .dynamicShape()
        .isValidSpawn((state, level, pos, type) -> false)
        .isRedstoneConductor((state, level, pos) -> false);
  }
}
