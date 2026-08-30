package com.faktocraft.common.block.impl.rubber_wood;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class RubberPlanks extends Block {

  public RubberPlanks(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties planksProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BROWN)
        .strength(2.0F)
        .sound(SoundType.WOOD)
        .ignitedByLava();
  }
}
