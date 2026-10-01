package com.faktocraft.common.block.impl.rubber_wood;

import com.faktocraft.Faktocraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class RubberSapling extends SaplingBlock {

  public static final ResourceKey<Feature> RUBBER_TREE_FEATURE = ResourceKey
      .create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Faktocraft.MODID, "rubber_tree"));

  public static final TreeGrower RUBBER_TREE_GROWER = new TreeGrower(Faktocraft.MODID + "_rubber",
      WeightedList.of(RUBBER_TREE_FEATURE), WeightedList.of(), WeightedList.of(), RUBBER_TREE_FEATURE);

  public RubberSapling(Properties properties) {
    super(RUBBER_TREE_GROWER, properties);
  }

  public static BlockBehaviour.Properties saplingProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.PLANT)
        .noCollision()
        .randomTicks()
        .instabreak()
        .sound(SoundType.GRASS)
        .pushReaction(PushReaction.POPPED);
  }
}
