package com.faktocraft.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessages {
  private PlayerMessages() {
  }

  public static void display(Player player, Component message, boolean overlay) {
    if (player instanceof ServerPlayer serverPlayer) {
      if (overlay) {
        serverPlayer.sendOverlayMessage(message);
      } else {
        serverPlayer.sendSystemMessage(message);
      }
      return;
    }
    if (overlay) {
      ClientProxy.get().displayOverlay(player, message);
    } else {
      player.sendSystemMessage(message);
    }
  }
}
