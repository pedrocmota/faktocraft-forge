package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.logistics.MenuModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketModuleTreeCount(String node, int count) implements CustomPacketPayload {

  public static final Type<PacketModuleTreeCount> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_module_tree_count"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketModuleTreeCount> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketModuleTreeCount::decode);

  @Override
  public Type<PacketModuleTreeCount> type() {
    return TYPE;
  }

  public static void encode(PacketModuleTreeCount msg, FriendlyByteBuf buf) {
    buf.writeUtf(msg.node, 192);
    buf.writeVarInt(msg.count);
  }

  public static PacketModuleTreeCount decode(FriendlyByteBuf buf) {
    return new PacketModuleTreeCount(buf.readUtf(192), buf.readVarInt());
  }

  public static void handle(PacketModuleTreeCount msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null) {
        return;
      }
      if (player.containerMenu instanceof MenuModule menu) {
        menu.applyTreeCount(msg.node, msg.count);
      }
    });
    ctx.setPacketHandled(true);
  }
}
