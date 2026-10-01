package com.faktocraft.common.world.feature.tree;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class RubberFoliagePlacer extends BlobFoliagePlacer {

  public static final MapCodec<RubberFoliagePlacer> CODEC = RecordCodecBuilder
      .mapCodec(i -> blobParts(i).apply(i, RubberFoliagePlacer::new));

  public RubberFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
    super(radius, offset, height);
  }

  @Override
  protected FoliagePlacerType<?> type() {
    return com.faktocraft.common.world.ModWorldGen.RUBBER_FOLIAGE_PLACER;
  }

  @Override
  protected void createFoliage(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter,
      RandomSource random, TreeFeature config, int treeHeight, FoliagePlacer.FoliageAttachment attachment,
      int foliageHeight, int leafRadius, int offset) {
    for (int yo = offset + 2; yo >= offset - foliageHeight - 1; yo--) {
      int currentRadius = Math.max(leafRadius + attachment.radiusOffsetXZ() - 1 - yo / 2, 0);
      if (yo >= 1) {
        currentRadius = 0;
      }
      this.placeLeavesRow(level, foliageSetter, random, config, attachment.pos(), currentRadius, yo,
          attachment.doubleTrunk());
    }
  }

  @Override
  protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius,
      boolean doubleTrunk) {
    return !(y >= 0 && dz == 0 && dx == 0) && dx == currentRadius && dz == currentRadius
        && (random.nextInt(2) == 0 || y == 0);
  }
}
