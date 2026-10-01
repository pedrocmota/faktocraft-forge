package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;

public record PacketTeleportCharge(int durationTicks, boolean dimensional, boolean active)
    implements CustomPacketPayload {

  public static final Type<PacketTeleportCharge> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_teleport_charge"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketTeleportCharge> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketTeleportCharge::decode);

  @Override
  public Type<PacketTeleportCharge> type() {
    return TYPE;
  }

  public static void encode(PacketTeleportCharge msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.durationTicks);
    buf.writeBoolean(msg.dimensional);
    buf.writeBoolean(msg.active);
  }

  public static PacketTeleportCharge decode(FriendlyByteBuf buf) {
    return new PacketTeleportCharge(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
  }

  public static void handle(PacketTeleportCharge msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
