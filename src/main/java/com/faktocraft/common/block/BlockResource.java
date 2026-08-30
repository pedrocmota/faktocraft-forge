package com.faktocraft.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockResource extends Block {

  public BlockResource(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties resourceProperties(float hardness, float resistance) {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.METAL)
        .strength(hardness, resistance);
  }
}
