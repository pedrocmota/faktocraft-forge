package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketTeleportFx() {

  public static final PacketTeleportFx INSTANCE = new PacketTeleportFx();

  public static void encode(PacketTeleportFx msg, FriendlyByteBuf buf) {
  }

  public static PacketTeleportFx decode(FriendlyByteBuf buf) {
    return INSTANCE;
  }

  public static void handle(PacketTeleportFx msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleTeleportFx(msg)));
    ctx.get().setPacketHandled(true);
  }
}
