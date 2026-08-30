package com.faktocraft.common.block.impl.rubber_wood;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class RubberSlab extends SlabBlock {

  public RubberSlab(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties slabProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BROWN)
        .strength(2.0F, 3.0F)
        .sound(SoundType.WOOD)
        .ignitedByLava();
  }
}
