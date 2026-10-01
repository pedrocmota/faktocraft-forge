package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.item.impl.armor.JetpackItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketJetpackInput(boolean thrust) implements CustomPacketPayload {

  public static final Type<PacketJetpackInput> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_jetpack_input"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketJetpackInput> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketJetpackInput::decode);

  @Override
  public Type<PacketJetpackInput> type() {
    return TYPE;
  }

  public static void encode(PacketJetpackInput msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.thrust);
  }

  public static PacketJetpackInput decode(FriendlyByteBuf buf) {
    return new PacketJetpackInput(buf.readBoolean());
  }

  public static void handle(PacketJetpackInput msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender != null) {
        sender.getPersistentData().putBoolean(JetpackItem.TAG_THRUST, msg.thrust);
      }
    });
    ctx.setPacketHandled(true);
  }
}
