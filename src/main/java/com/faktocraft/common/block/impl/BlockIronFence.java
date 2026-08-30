package com.faktocraft.common.block.impl;

import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockIronFence extends FenceBlock {

  public BlockIronFence(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties fenceProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.METAL)
        .strength(3F, 5F)
        .sound(SoundType.METAL);
  }
}
