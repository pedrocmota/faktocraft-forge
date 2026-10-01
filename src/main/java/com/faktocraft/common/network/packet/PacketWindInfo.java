package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;

public record PacketWindInfo(int windPercent, int y, int estimate) implements CustomPacketPayload {

  public static final Type<PacketWindInfo> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_wind_info"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketWindInfo> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketWindInfo::decode);

  @Override
  public Type<PacketWindInfo> type() {
    return TYPE;
  }

  public static void encode(PacketWindInfo msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.windPercent);
    buf.writeInt(msg.y);
    buf.writeInt(msg.estimate);
  }

  public static PacketWindInfo decode(FriendlyByteBuf buf) {
    return new PacketWindInfo(buf.readInt(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketWindInfo msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
