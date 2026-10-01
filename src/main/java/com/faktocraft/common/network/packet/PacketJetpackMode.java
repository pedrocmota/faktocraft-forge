package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.item.impl.armor.AdvancedJetpackItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketJetpackMode() implements CustomPacketPayload {

  public static final Type<PacketJetpackMode> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_jetpack_mode"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketJetpackMode> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketJetpackMode::decode);

  @Override
  public Type<PacketJetpackMode> type() {
    return TYPE;
  }

  public static final PacketJetpackMode INSTANCE = new PacketJetpackMode();

  public static void encode(PacketJetpackMode msg, FriendlyByteBuf buf) {
  }

  public static PacketJetpackMode decode(FriendlyByteBuf buf) {
    return INSTANCE;
  }

  public static void handle(PacketJetpackMode msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender != null) {
        AdvancedJetpackItem.toggleMode(sender);
      }
    });
    ctx.setPacketHandled(true);
  }
}
