package com.faktocraft.common.network.packet;

import com.faktocraft.common.item.impl.armor.JetpackItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketJetpackInput(boolean thrust) {

  public static void encode(PacketJetpackInput msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.thrust);
  }

  public static PacketJetpackInput decode(FriendlyByteBuf buf) {
    return new PacketJetpackInput(buf.readBoolean());
  }

  public static void handle(PacketJetpackInput msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender != null) {
        sender.getPersistentData().putBoolean(JetpackItem.TAG_THRUST, msg.thrust);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
