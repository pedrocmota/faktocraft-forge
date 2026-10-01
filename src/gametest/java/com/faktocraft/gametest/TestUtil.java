package com.faktocraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

public final class TestUtil {
  private TestUtil() {
  }

  @Nullable
  public static BlockEntity blockEntity(GameTestHelper helper, BlockPos relativePos) {
    return helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
  }

  public static GameTestAssertException assertion(GameTestHelper helper, String message) {
    return helper.assertionException(Component.literal(message));
  }

  public static GameTestAssertException assertion(String message) {
    return new GameTestAssertException(Component.literal(message), 0);
  }

  public static boolean blockChunkForced(ServerLevel level, long chunk) {
    TicketStorage storage = level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
    for (Ticket ticket : storage.getTickets(chunk)) {
      if (ticket.getType() == NeoForgeMod.BLOCK_TICKET.value()
          || ticket.getType() == NeoForgeMod.BLOCK_WITH_NATURAL_SPAWNING_TICKET.value()) {
        return true;
      }
    }
    return false;
  }
}
