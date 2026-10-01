package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketScannerSaveScan(BlockPos blockPos) implements CustomPacketPayload {

  public static final Type<PacketScannerSaveScan> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_scanner_save_scan"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketScannerSaveScan> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketScannerSaveScan::decode);

  @Override
  public Type<PacketScannerSaveScan> type() {
    return TYPE;
  }

  public static void encode(PacketScannerSaveScan msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketScannerSaveScan decode(FriendlyByteBuf buf) {
    return new PacketScannerSaveScan(buf.readBlockPos());
  }

  public static void handle(PacketScannerSaveScan msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.IScannerActions scanner) {
          scanner.saveScan();
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
