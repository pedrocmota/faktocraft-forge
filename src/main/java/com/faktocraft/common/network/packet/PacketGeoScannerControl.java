package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketGeoScannerControl(BlockPos blockPos, boolean running) implements CustomPacketPayload {

  public static final Type<PacketGeoScannerControl> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_geo_scanner_control"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketGeoScannerControl> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketGeoScannerControl::decode);

  @Override
  public Type<PacketGeoScannerControl> type() {
    return TYPE;
  }

  public static void encode(PacketGeoScannerControl msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.running);
  }

  public static PacketGeoScannerControl decode(FriendlyByteBuf buf) {
    return new PacketGeoScannerControl(buf.readBlockPos(), buf.readBoolean());
  }

  public static void handle(PacketGeoScannerControl msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof BlockEntityGeoScanner scanner) {
          scanner.setRunning(msg.running());
          ModNetworking.sendToPlayer(player, PacketGeoScannerState.of(scanner, false, false));
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
