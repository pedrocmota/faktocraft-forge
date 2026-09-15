package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.monitor.StatusBridges;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class StatusClientBridges {

  private static final List<StatusClientBridge> BRIDGES = new ArrayList<>();
  private static final String JADE_CLIENT = "com.faktocraft.integration.jade.JadeStatusClient";

  private StatusClientBridges() {
  }

  public static void init() {
    if (ModList.get().isLoaded("jade")) {
      Object bridge = StatusBridges.load(JADE_CLIENT);
      if (bridge instanceof StatusClientBridge clientBridge) {
        BRIDGES.add(clientBridge);
      }
    }
  }

  public static boolean isEmpty() {
    return BRIDGES.isEmpty();
  }

  @Nullable
  public static Object build(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
      CompoundTag data) {
    for (StatusClientBridge bridge : BRIDGES) {
      try {
        Object built = bridge.build(level, pos, state, blockEntity, data);
        if (built != null) {
          return new Built(bridge, built);
        }
      } catch (RuntimeException ignored) {
        continue;
      }
    }
    return null;
  }

  public static boolean render(Object built, GuiGraphics graphics, int width, int height) {
    if (!(built instanceof Built entry)) {
      return false;
    }
    try {
      return entry.bridge().render(entry.value(), graphics, width, height);
    } catch (RuntimeException e) {
      return false;
    }
  }

  private record Built(StatusClientBridge bridge, Object value) {
  }
}
