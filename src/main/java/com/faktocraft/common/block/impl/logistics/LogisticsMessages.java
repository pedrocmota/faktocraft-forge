package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketTableMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class LogisticsMessages {

  private LogisticsMessages() {
  }

  public static void error(@Nullable ServerPlayer player, Component message) {
    if (player == null) {
      return;
    }
    if (player.containerMenu instanceof MenuRequestTable) {
      ModNetworking.sendToPlayer(player, new PacketTableMessage(message, true));
    } else {
      player.displayClientMessage(message, true);
    }
  }
}
