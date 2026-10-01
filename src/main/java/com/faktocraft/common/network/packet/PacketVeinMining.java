package com.faktocraft.common.network.packet;

import com.faktocraft.common.item.impl.tools.VeinMining;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketVeinMining(boolean active) {

  public static void encode(PacketVeinMining msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.active);
  }

  public static PacketVeinMining decode(FriendlyByteBuf buf) {
    return new PacketVeinMining(buf.readBoolean());
  }

  public static void handle(PacketVeinMining msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender != null) {
        VeinMining.setActive(sender, msg.active);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
