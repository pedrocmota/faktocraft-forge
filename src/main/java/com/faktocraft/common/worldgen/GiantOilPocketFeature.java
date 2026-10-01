package com.faktocraft.common.worldgen;

import com.faktocraft.common.fluid.ModFluids;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public class GiantOilPocketFeature implements Feature {

  public static final MapCodec<GiantOilPocketFeature> CODEC = MapCodec.unit(GiantOilPocketFeature::new);

  public GiantOilPocketFeature() {
  }

  @Override
  public MapCodec<GiantOilPocketFeature> codec() {
    return CODEC;
  }

  private record Blob(double cx, double cy, double cz, double rx, double ry, double rz) {
    double dist(int dx, int dy, int dz) {
      double nx = (dx - cx) / rx;
      double ny = (dy - cy) / ry;
      double nz = (dz - cz) / rz;
      return nx * nx + ny * ny + nz * nz;
    }
  }

  @Override
  public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
    if (origin.getY() - 10 <= level.getMinY()) {
      return false;
    }

    java.util.List<Blob> blobs = new java.util.ArrayList<>();
    blobs.add(new Blob(0, 0, 0,
        8 + random.nextInt(3), 5 + random.nextInt(2), 8 + random.nextInt(3)));
    int extra = 4 + random.nextInt(4);
    for (int i = 0; i < extra; i++) {
      double ox = (random.nextDouble() * 2 - 1) * 5;
      double oy = (random.nextDouble() * 2 - 1) * 2.5;
      double oz = (random.nextDouble() * 2 - 1) * 5;
      blobs.add(new Blob(ox, oy, oz,
          5 + random.nextInt(5), 4 + random.nextInt(3), 5 + random.nextInt(5)));
    }

    BlockState oil = ModFluids.OIL.block().defaultBlockState();
    BlockState stone = Blocks.STONE.defaultBlockState();
    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    boolean placedAny = false;
    for (int dx = -16; dx <= 16; dx++) {
      for (int dy = -10; dy <= 10; dy++) {
        for (int dz = -16; dz <= 16; dz++) {
          double d = Double.MAX_VALUE;
          for (Blob blob : blobs) {
            d = Math.min(d, blob.dist(dx, dy, dz));
            if (d <= 1.0) {
              break;
            }
          }
          if (d > 1.4) {
            continue;
          }
          cursor.setWithOffset(origin, dx, dy, dz);
          BlockState current = level.getBlockState(cursor);
          if (current.is(Blocks.BEDROCK) || current.hasBlockEntity()) {
            continue;
          }
          if (d <= 1.0) {
            level.setBlock(cursor, oil, 2);
            placedAny = true;
          } else if (current.isAir() || !current.getFluidState().isEmpty()) {
            level.setBlock(cursor, stone, 2);
          }
        }
      }
    }
    if (placedAny) {
      level.scheduleTick(origin, ModFluids.OIL.still(), 10);
    }
    return placedAny;
  }
}
