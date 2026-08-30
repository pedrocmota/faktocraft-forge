package com.faktocraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketIEMeterInfo(boolean network, int tierLvl, int voltageLvl, long transfer,
    int generators, int machines, int batteries, long stored, long capacity,
    boolean showGenerated, long lastGenerated, long totalGenerated,
    boolean showConsumed, long lastConsumed, long totalConsumed,
    long netInput, long netOutput, long netDemand, long receiveRate) {

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

  public static void handle(PacketIEMeterInfo msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleIEMeterInfo(msg)));
    ctx.get().setPacketHandled(true);
  }
}
