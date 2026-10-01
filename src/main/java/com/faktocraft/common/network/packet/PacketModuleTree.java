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

public record PacketModuleTree(String node, boolean allow) implements CustomPacketPayload {

  public static final Type<PacketModuleTree> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_module_tree"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketModuleTree> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketModuleTree::decode);

  @Override
  public Type<PacketModuleTree> type() {
    return TYPE;
  }

  public static void encode(PacketModuleTree msg, FriendlyByteBuf buf) {
    buf.writeUtf(msg.node, 192);
    buf.writeBoolean(msg.allow);
  }

  public static PacketModuleTree decode(FriendlyByteBuf buf) {
    return new PacketModuleTree(buf.readUtf(192), buf.readBoolean());
  }

  public static void handle(PacketModuleTree msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null) {
        return;
      }
      if (player.containerMenu instanceof MenuModule menu) {
        menu.applyTreeNode(msg.node, msg.allow);
      }
    });
    ctx.setPacketHandled(true);
  }
}
