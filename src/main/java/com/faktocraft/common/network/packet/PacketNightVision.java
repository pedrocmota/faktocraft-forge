package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketNightVision() implements CustomPacketPayload {

  public static final Type<PacketNightVision> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_night_vision"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketNightVision> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketNightVision::decode);

  @Override
  public Type<PacketNightVision> type() {
    return TYPE;
  }

  public static final PacketNightVision INSTANCE = new PacketNightVision();

  public static void encode(PacketNightVision msg, FriendlyByteBuf buf) {
  }

  public static PacketNightVision decode(FriendlyByteBuf buf) {
    return INSTANCE;
  }

  public static void handle(PacketNightVision msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      com.faktocraft.common.util.NightVisionHandler.toggle(sender);
    });
    ctx.setPacketHandled(true);
  }
}
