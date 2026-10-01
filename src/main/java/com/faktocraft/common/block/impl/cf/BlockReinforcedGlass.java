package com.faktocraft.common.block.impl.cf;

import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockReinforcedGlass extends HalfTransparentBlock {

  public BlockReinforcedGlass(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties reinforcedGlassProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BROWN)
        .strength(10.0F, 10000000000F)
        .sound(SoundType.GLASS)
        .noOcclusion()
        .isValidSpawn((state, level, pos, type) -> false)
        .isSuffocating((state, level, pos) -> false)
        .isViewBlocking((state, level, pos, box) -> false)
        .isRedstoneConductor((state, level, pos) -> false);
  }
}
