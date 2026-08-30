package com.faktocraft.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockOre extends Block {

  public BlockOre(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties oreProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.STONE)
        .requiresCorrectToolForDrops()
        .strength(3F, 5F);
  }
}
