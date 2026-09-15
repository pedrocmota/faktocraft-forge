package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.network.NbtPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

public record PacketGeoScannerState(BlockPos blockPos, boolean openScreen, int revision, boolean running,
    int energy, int capacity, int scanned, boolean jobActive, int jobCx, int jobCz, int jobRemaining,
    boolean manualPending, int manualCx, int manualCz, int code, @Nullable CompoundTag scans) {

  public static PacketGeoScannerState of(BlockEntityGeoScanner scanner, boolean openScreen, boolean withScans) {
    return new PacketGeoScannerState(scanner.getBlockPos(), openScreen, scanner.getRevision(),
        scanner.isRunning(), scanner.getEnergyStorage().energyStored(), BlockEntityGeoScanner.ENERGY_CAPACITY,
        scanner.getScannedCount(), scanner.isJobActive(), scanner.getJobCx(), scanner.getJobCz(),
        scanner.getJobRemaining(), scanner.isManualPending(), scanner.getManualCx(), scanner.getManualCz(),
        scanner.getCode(), withScans ? scanner.collectScans() : null);
  }

  public static void encode(PacketGeoScannerState msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.openScreen);
    buf.writeInt(msg.revision);
    buf.writeBoolean(msg.running);
    buf.writeInt(msg.energy);
    buf.writeInt(msg.capacity);
    buf.writeInt(msg.scanned);
    buf.writeBoolean(msg.jobActive);
    buf.writeInt(msg.jobCx);
    buf.writeInt(msg.jobCz);
    buf.writeInt(msg.jobRemaining);
    buf.writeBoolean(msg.manualPending);
    buf.writeInt(msg.manualCx);
    buf.writeInt(msg.manualCz);
    buf.writeVarInt(msg.code);
    NbtPayload.write(buf, msg.scans);
  }

  public static PacketGeoScannerState decode(FriendlyByteBuf buf) {
    return new PacketGeoScannerState(buf.readBlockPos(), buf.readBoolean(), buf.readInt(), buf.readBoolean(),
        buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt(), buf.readInt(),
        buf.readInt(), buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readVarInt(), NbtPayload.read(buf));
  }

  public static void handle(PacketGeoScannerState msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleGeoScannerState(msg)));
    ctx.get().setPacketHandled(true);
  }
}
