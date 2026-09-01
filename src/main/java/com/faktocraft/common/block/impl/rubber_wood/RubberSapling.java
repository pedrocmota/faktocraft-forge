package com.faktocraft.common.block.impl.rubber_wood;

import com.faktocraft.Faktocraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public class RubberSapling extends SaplingBlock {

  public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_FEATURE = ResourceKey
      .create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Faktocraft.MODID, "rubber_tree"));

  public static final AbstractTreeGrower RUBBER_TREE_GROWER = new AbstractTreeGrower() {
    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
      return RUBBER_TREE_FEATURE;
    }
  };

  public RubberSapling(Properties properties) {
    super(RUBBER_TREE_GROWER, properties);
  }

  public static BlockBehaviour.Properties saplingProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.PLANT)
        .noCollission()
        .randomTicks()
        .instabreak()
        .sound(SoundType.GRASS)
        .pushReaction(PushReaction.DESTROY);
  }
}
