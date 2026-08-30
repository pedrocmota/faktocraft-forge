package com.faktocraft.common.block.impl.rubber_wood;

import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class RubberStairs extends StairBlock {

  public RubberStairs(Properties properties) {
    super(() -> ModBlocks.RUBBER_PLANKS.defaultBlockState(), properties);
  }

  public static BlockBehaviour.Properties stairsProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BROWN)
        .strength(2.0F, 3.0F)
        .sound(SoundType.WOOD)
        .ignitedByLava();
  }
}
