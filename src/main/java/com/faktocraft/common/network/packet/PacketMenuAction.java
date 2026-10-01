package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketMenuAction(int containerId, int actionId) implements CustomPacketPayload {

  public static final Type<PacketMenuAction> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_menu_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketMenuAction> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketMenuAction::decode);

  @Override
  public Type<PacketMenuAction> type() {
    return TYPE;
  }

  public static void encode(PacketMenuAction msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.containerId);
    buf.writeVarInt(msg.actionId);
  }

  public static PacketMenuAction decode(FriendlyByteBuf buf) {
    return new PacketMenuAction(buf.readVarInt(), buf.readVarInt());
  }

  public static void handle(PacketMenuAction msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null || player.containerMenu.containerId != msg.containerId
          || !player.containerMenu.stillValid(player)) {
        return;
      }
      if (player.containerMenu.clickMenuButton(player, msg.actionId)) {
        player.containerMenu.broadcastChanges();
      }
    });
    ctx.setPacketHandled(true);
  }
}
