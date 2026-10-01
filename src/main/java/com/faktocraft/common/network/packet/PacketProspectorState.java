package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.network.NbtPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record PacketProspectorState(int code, int revision, CompoundTag scans) implements CustomPacketPayload {

  public static final Type<PacketProspectorState> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_prospector_state"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketProspectorState> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketProspectorState::decode);

  @Override
  public Type<PacketProspectorState> type() {
    return TYPE;
  }

  public static void encode(PacketProspectorState msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.code);
    buf.writeInt(msg.revision);
    NbtPayload.write(buf, msg.scans);
  }

  public static PacketProspectorState decode(FriendlyByteBuf buf) {
    int code = buf.readVarInt();
    int revision = buf.readInt();
    CompoundTag scans = NbtPayload.read(buf);
    return new PacketProspectorState(code, revision, scans == null ? new CompoundTag() : scans);
  }

  public static void handle(PacketProspectorState msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
