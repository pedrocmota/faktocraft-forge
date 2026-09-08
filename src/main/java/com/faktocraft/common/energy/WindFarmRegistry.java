package com.faktocraft.common.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WindFarmRegistry {

  private static final Map<ResourceKey<Level>, Set<BlockPos>> GENERATORS = new ConcurrentHashMap<>();

  private WindFarmRegistry() {
  }

  public static void add(Level level, BlockPos pos) {
    GENERATORS.computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
  }

  public static void clear(ResourceKey<Level> dimension) {
    GENERATORS.remove(dimension);
  }

  public static void remove(Level level, BlockPos pos) {
    Set<BlockPos> set = GENERATORS.get(level.dimension());
    if (set != null) {
      set.remove(pos);
    }
  }

  public static int countNear(Level level, BlockPos pos, double radius) {
    Set<BlockPos> set = GENERATORS.get(level.dimension());
    if (set == null) {
      return 0;
    }
    double radiusSq = radius * radius;
    int count = 0;
    for (BlockPos other : set) {
      if (!other.equals(pos) && other.distSqr(pos) < radiusSq) {
        count++;
      }
    }
    return count;
  }
}
