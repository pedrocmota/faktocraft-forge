package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketTeleportFx(boolean dimensional) {

  public static void encode(PacketTeleportFx msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.dimensional);
  }

  public static PacketTeleportFx decode(FriendlyByteBuf buf) {
    return new PacketTeleportFx(buf.readBoolean());
  }

  public static void handle(PacketTeleportFx msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleTeleportFx(msg)));
    ctx.get().setPacketHandled(true);
  }
}
