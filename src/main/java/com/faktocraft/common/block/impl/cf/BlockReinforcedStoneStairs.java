package com.faktocraft.common.block.impl.cf;

import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockReinforcedStoneStairs extends StairBlock {

  public BlockReinforcedStoneStairs(Properties properties) {
    super(() -> ModBlocks.REINFORCED_STONE.defaultBlockState(), properties);
  }

  public static BlockBehaviour.Properties reinforcedStoneStairsProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.STONE)
        .strength(10.0F, 10000000000F)
        .sound(SoundType.STONE);
  }
}
