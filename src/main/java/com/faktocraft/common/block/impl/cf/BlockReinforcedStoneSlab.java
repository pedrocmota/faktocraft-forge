package com.faktocraft.common.block.impl.cf;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockReinforcedStoneSlab extends SlabBlock {

  public BlockReinforcedStoneSlab(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties reinforcedStoneSlabProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.STONE)
        .strength(10.0F, 10000000000F)
        .sound(SoundType.STONE);
  }
}
