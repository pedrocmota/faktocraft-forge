package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;

public record PacketIEMeterInfo(boolean network, int tierLvl, int voltageLvl, long transfer,
    int generators, int machines, int batteries, long stored, long capacity,
    boolean showGenerated, long lastGenerated, long totalGenerated,
    boolean showConsumed, long lastConsumed, long totalConsumed,
    long netInput, long netOutput, long netDemand, long receiveRate) implements CustomPacketPayload {

  public static final Type<PacketIEMeterInfo> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_i_e_meter_info"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketIEMeterInfo> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketIEMeterInfo::decode);

  @Override
  public Type<PacketIEMeterInfo> type() {
    return TYPE;
  }

  public static void encode(PacketIEMeterInfo msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.network);
    buf.writeInt(msg.tierLvl);
    buf.writeInt(msg.voltageLvl);
    buf.writeLong(msg.transfer);
    buf.writeInt(msg.generators);
    buf.writeInt(msg.machines);
    buf.writeInt(msg.batteries);
    buf.writeLong(msg.stored);
    buf.writeLong(msg.capacity);
    buf.writeBoolean(msg.showGenerated);
    buf.writeLong(msg.lastGenerated);
    buf.writeLong(msg.totalGenerated);
    buf.writeBoolean(msg.showConsumed);
    buf.writeLong(msg.lastConsumed);
    buf.writeLong(msg.totalConsumed);
    buf.writeLong(msg.netInput);
    buf.writeLong(msg.netOutput);
    buf.writeLong(msg.netDemand);
    buf.writeLong(msg.receiveRate);
  }

  public static PacketIEMeterInfo decode(FriendlyByteBuf buf) {
    return new PacketIEMeterInfo(buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readLong(),
        buf.readInt(), buf.readInt(), buf.readInt(), buf.readLong(), buf.readLong(),
        buf.readBoolean(), buf.readLong(), buf.readLong(),
        buf.readBoolean(), buf.readLong(), buf.readLong(),
        buf.readLong(), buf.readLong(), buf.readLong(), buf.readLong());
  }

  public static void handle(PacketIEMeterInfo msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
