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

public record PacketGeoScannerManual(BlockPos blockPos, int chunkX, int chunkZ) implements CustomPacketPayload {

  public static final Type<PacketGeoScannerManual> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_geo_scanner_manual"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketGeoScannerManual> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketGeoScannerManual::decode);

  @Override
  public Type<PacketGeoScannerManual> type() {
    return TYPE;
  }

  public static void encode(PacketGeoScannerManual msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.chunkX);
    buf.writeInt(msg.chunkZ);
  }

  public static PacketGeoScannerManual decode(FriendlyByteBuf buf) {
    return new PacketGeoScannerManual(buf.readBlockPos(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketGeoScannerManual msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof BlockEntityGeoScanner scanner) {
          scanner.setManualTarget(msg.chunkX(), msg.chunkZ());
          ModNetworking.sendToPlayer(player, PacketGeoScannerState.of(scanner, false, false));
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
