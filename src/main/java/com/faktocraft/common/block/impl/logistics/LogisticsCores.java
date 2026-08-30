package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LogisticsCores {

  private static final Map<Level, Set<BlockEntityLogisticsController>> CORES = new ConcurrentHashMap<>();

  private LogisticsCores() {
  }

  public static void register(BlockEntityLogisticsController core) {
    if (core.getLevel() != null && !core.getLevel().isClientSide()) {
      CORES.computeIfAbsent(core.getLevel(), l -> ConcurrentHashMap.newKeySet()).add(core);
    }
  }

  public static void unregister(BlockEntityLogisticsController core) {
    if (core.getLevel() != null) {
      Set<BlockEntityLogisticsController> set = CORES.get(core.getLevel());
      if (set != null) {
        set.remove(core);
      }
    }
  }

  public static void markDirtyNear(Level level, BlockPos pos) {
    Set<BlockEntityLogisticsController> set = CORES.get(level);
    if (set == null) {
      return;
    }
    for (BlockEntityLogisticsController core : set) {
      LogisticsGraph graph = core.graphIfPresent();
      if (graph == null || graph.touches(pos)) {
        core.markGraphDirty();
      }
    }
  }

  @Nullable
  public static BlockEntityLogisticsController coreFor(Level level, BlockPos memberPos) {
    Set<BlockEntityLogisticsController> set = CORES.get(level);
    if (set == null) {
      return null;
    }
    for (BlockEntityLogisticsController core : set) {
      LogisticsGraph graph = core.graph();
      if (graph != null && graph.contains(memberPos)) {
        return core;
      }
    }
    return null;
  }
}
