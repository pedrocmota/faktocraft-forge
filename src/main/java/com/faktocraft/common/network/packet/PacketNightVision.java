package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketNightVision() {

  public static final PacketNightVision INSTANCE = new PacketNightVision();

  public static void encode(PacketNightVision msg, FriendlyByteBuf buf) {
  }

  public static PacketNightVision decode(FriendlyByteBuf buf) {
    return INSTANCE;
  }

  public static void handle(PacketNightVision msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      com.faktocraft.common.util.NightVisionHandler.toggle(sender);
    });
    ctx.get().setPacketHandled(true);
  }
}
