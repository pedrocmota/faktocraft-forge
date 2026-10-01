package com.faktocraft.common.network.packet;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.impl.tools.VeinMining;
import com.faktocraft.common.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record PacketVeinMining(boolean active) implements CustomPacketPayload {

  public static final Type<PacketVeinMining> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_vein_mining"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketVeinMining> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketVeinMining::decode);

  @Override
  public Type<PacketVeinMining> type() {
    return TYPE;
  }

  public static void encode(PacketVeinMining msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.active);
  }

  public static PacketVeinMining decode(FriendlyByteBuf buf) {
    return new PacketVeinMining(buf.readBoolean());
  }

  public static void handle(PacketVeinMining msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender != null) {
        VeinMining.setActive(sender, msg.active);
      }
    });
    ctx.setPacketHandled(true);
  }
}
