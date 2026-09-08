package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketTeleportCharge(int durationTicks, boolean dimensional, boolean active) {

  public static void encode(PacketTeleportCharge msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.durationTicks);
    buf.writeBoolean(msg.dimensional);
    buf.writeBoolean(msg.active);
  }

  public static PacketTeleportCharge decode(FriendlyByteBuf buf) {
    return new PacketTeleportCharge(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
  }

  public static void handle(PacketTeleportCharge msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleTeleportCharge(msg)));
    ctx.get().setPacketHandled(true);
  }
}
