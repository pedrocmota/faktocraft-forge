package com.faktocraft.common.block.impl.rubber_wood;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class RubberLeaves extends LeavesBlock {

  public RubberLeaves(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties leavesProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.PLANT)
        .strength(0.2F)
        .randomTicks()
        .sound(SoundType.GRASS)
        .noOcclusion()
        .isValidSpawn((state, level, pos, type) -> false)
        .isSuffocating((state, level, pos) -> false)
        .isViewBlocking((state, level, pos) -> false)
        .ignitedByLava()
        .pushReaction(PushReaction.DESTROY)
        .isRedstoneConductor((state, level, pos) -> false);
  }
}
