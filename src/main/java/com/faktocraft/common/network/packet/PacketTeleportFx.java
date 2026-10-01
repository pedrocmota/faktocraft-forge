package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;

public record PacketTeleportFx(boolean dimensional) implements CustomPacketPayload {

  public static final Type<PacketTeleportFx> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_teleport_fx"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketTeleportFx> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketTeleportFx::decode);

  @Override
  public Type<PacketTeleportFx> type() {
    return TYPE;
  }

  public static void encode(PacketTeleportFx msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.dimensional);
  }

  public static PacketTeleportFx decode(FriendlyByteBuf buf) {
    return new PacketTeleportFx(buf.readBoolean());
  }

  public static void handle(PacketTeleportFx msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
