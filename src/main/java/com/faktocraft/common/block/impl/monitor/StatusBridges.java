package com.faktocraft.common.block.impl.monitor;

import com.faktocraft.Faktocraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class StatusBridges {

  private static final List<StatusBridge> BRIDGES = new ArrayList<>();
  private static final String JADE_BRIDGE = "com.faktocraft.integration.jade.JadeStatusBridge";

  private StatusBridges() {
  }

  public static void init() {
    if (ModList.get().isLoaded("jade")) {
      Object bridge = load(JADE_BRIDGE);
      if (bridge instanceof StatusBridge statusBridge) {
        BRIDGES.add(statusBridge);
      }
    }
  }

  @Nullable
  public static Object load(String className) {
    try {
      return Class.forName(className).getMethod("create").invoke(null);
    } catch (ReflectiveOperationException | LinkageError e) {
      Faktocraft.LOGGER.warn("Status monitor bridge {} unavailable: {}", className, e.toString());
      return null;
    }
  }

  static void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
      CompoundTag out) {
    for (StatusBridge bridge : BRIDGES) {
      try {
        bridge.collect(level, pos, state, blockEntity, out);
      } catch (RuntimeException e) {
        Faktocraft.LOGGER.debug("Status bridge failed to collect at {}: {}", pos, e.toString());
      }
    }
  }
}
