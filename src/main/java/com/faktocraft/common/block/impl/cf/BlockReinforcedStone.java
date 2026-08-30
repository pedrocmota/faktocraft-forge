package com.faktocraft.common.block.impl.cf;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockReinforcedStone extends Block {

  public BlockReinforcedStone(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties reinforcedStoneProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.STONE)
        .strength(10.0F, 10000000000F)
        .sound(SoundType.STONE);
  }
}
