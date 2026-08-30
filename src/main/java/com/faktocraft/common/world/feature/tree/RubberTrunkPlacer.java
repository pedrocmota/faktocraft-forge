package com.faktocraft.common.world.feature.tree;

import com.faktocraft.common.util.BlockStateHelper;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class RubberTrunkPlacer extends StraightTrunkPlacer {

  public static final Codec<RubberTrunkPlacer> CODEC = RecordCodecBuilder
      .create(i -> trunkPlacerParts(i).apply(i, RubberTrunkPlacer::new));

  public RubberTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
    super(baseHeight, heightRandA, heightRandB);
  }

  @Override
  protected TrunkPlacerType<?> type() {
    return com.faktocraft.common.world.ModWorldGen.RUBBER_TRUNK_PLACER;
  }

  @Override
  public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level,
      BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int height, BlockPos pos,
      TreeConfiguration config) {
    setDirtAt(level, blockSetter, random, pos.below(), config);

    int exposed = Math.max(1, height - 3);
    Set<Integer> resinSpots = new HashSet<>();
    int spotCount = Math.min(exposed, random.nextFloat() < 0.3F ? 2 : 1);
    while (resinSpots.size() < spotCount) {
      resinSpots.add(random.nextInt(exposed));
    }

    for (int i = 0; i < height; i++) {
      boolean spot = resinSpots.contains(i);
      boolean wet = spot && random.nextBoolean();
      placeLog(level, blockSetter, random, pos.above(i), config, state -> {
        if (!spot || !state.hasProperty(BlockStateHelper.wetProperty)) {
          return state;
        }
        return wet ? state.setValue(BlockStateHelper.wetProperty, true)
            : state.setValue(BlockStateHelper.dryProperty, true);
      });
    }
    return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(height), 0, false));
  }
}
