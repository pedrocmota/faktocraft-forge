package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketWindInfo(int windPercent, int y, int estimate) {

  public static void encode(PacketWindInfo msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.windPercent);
    buf.writeInt(msg.y);
    buf.writeInt(msg.estimate);
  }

  public static PacketWindInfo decode(FriendlyByteBuf buf) {
    return new PacketWindInfo(buf.readInt(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketWindInfo msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleWindInfo(msg)));
    ctx.get().setPacketHandled(true);
  }
}
