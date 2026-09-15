package com.faktocraft.common.block;

import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockRubberCarpet extends CarpetBlock {

  public BlockRubberCarpet(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties carpetProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BLACK)
        .strength(0.1F)
        .sound(SoundType.SLIME_BLOCK);
  }

  public static BlockBehaviour.Properties blockProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BLACK)
        .strength(0.8F)
        .sound(SoundType.SLIME_BLOCK);
  }
}
