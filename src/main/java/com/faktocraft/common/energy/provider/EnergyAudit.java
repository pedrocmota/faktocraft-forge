package com.faktocraft.common.energy.provider;

import com.faktocraft.IndReb;
import com.faktocraft.common.block.impl.cable.BlockCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EnergyAudit {

  public static final boolean ENABLED = Boolean.getBoolean("faktocraft.energyAudit");

  private static final Set<String> reported = ConcurrentHashMap.newKeySet();

  private EnergyAudit() {
  }

  public static void check(EnergyNetworks networks, String where) {
    if (!ENABLED) {
      return;
    }
    Level level = networks.getLevel();
    Map<BlockPos, EnergyNetwork> owner = new HashMap<>();
    for (EnergyNetwork network : networks.getNetworks()) {
      for (BlockPos pos : network.getConnections()) {
        EnergyNetwork previous = owner.put(pos, network);
        if (previous != null && previous != network) {
          report(where, "position claimed by two networks", pos);
        }

        if (level.isLoaded(pos) && !(level.getBlockState(pos).getBlock() instanceof BlockCable)) {
          report(where, "connection without a cable ("
              + level.getBlockState(pos).getBlock() + ")", pos);
        }
      }
    }
  }

  private static void report(String where, String what, BlockPos pos) {
    if (!reported.add(where + "|" + what)) {
      return;
    }
    IndReb.LOGGER.error("[energyAudit] {} after {}, at {}", what, where, pos,
        new Throwable("energyAudit trace"));
  }
}
