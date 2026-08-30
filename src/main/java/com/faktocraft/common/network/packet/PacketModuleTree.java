package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.logistics.MenuModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketModuleTree(String node, boolean allow) {

  public static void encode(PacketModuleTree msg, FriendlyByteBuf buf) {
    buf.writeUtf(msg.node, 192);
    buf.writeBoolean(msg.allow);
  }

  public static PacketModuleTree decode(FriendlyByteBuf buf) {
    return new PacketModuleTree(buf.readUtf(192), buf.readBoolean());
  }

  public static void handle(PacketModuleTree msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) {
        return;
      }
      if (player.containerMenu instanceof MenuModule menu) {
        menu.applyTreeNode(msg.node, msg.allow);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
