package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.logistics.MenuModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketModuleTreeCount(String node, int count) {

  public static void encode(PacketModuleTreeCount msg, FriendlyByteBuf buf) {
    buf.writeUtf(msg.node, 192);
    buf.writeVarInt(msg.count);
  }

  public static PacketModuleTreeCount decode(FriendlyByteBuf buf) {
    return new PacketModuleTreeCount(buf.readUtf(192), buf.readVarInt());
  }

  public static void handle(PacketModuleTreeCount msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) {
        return;
      }
      if (player.containerMenu instanceof MenuModule menu) {
        menu.applyTreeCount(msg.node, msg.count);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
