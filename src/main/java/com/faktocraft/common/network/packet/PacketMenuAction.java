package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketMenuAction(int containerId, int actionId) {

  public static void encode(PacketMenuAction msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.containerId);
    buf.writeVarInt(msg.actionId);
  }

  public static PacketMenuAction decode(FriendlyByteBuf buf) {
    return new PacketMenuAction(buf.readVarInt(), buf.readVarInt());
  }

  public static void handle(PacketMenuAction msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null || player.containerMenu.containerId != msg.containerId
          || !player.containerMenu.stillValid(player)) {
        return;
      }
      if (player.containerMenu.clickMenuButton(player, msg.actionId)) {
        player.containerMenu.broadcastChanges();
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
