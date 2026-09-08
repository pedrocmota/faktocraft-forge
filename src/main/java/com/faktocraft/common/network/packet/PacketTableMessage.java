package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketTableMessage(Component message, boolean error) {

  public static void encode(PacketTableMessage msg, FriendlyByteBuf buf) {
    buf.writeComponent(msg.message);
    buf.writeBoolean(msg.error);
  }

  public static PacketTableMessage decode(FriendlyByteBuf buf) {
    return new PacketTableMessage(buf.readComponent(), buf.readBoolean());
  }

  public static void handle(PacketTableMessage msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleTableMessage(msg)));
    ctx.get().setPacketHandled(true);
  }
}
