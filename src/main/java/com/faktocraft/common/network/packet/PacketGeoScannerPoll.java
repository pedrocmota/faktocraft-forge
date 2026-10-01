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

public record PacketGeoScannerPoll(BlockPos blockPos, int knownRevision) implements CustomPacketPayload {

  public static final Type<PacketGeoScannerPoll> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_geo_scanner_poll"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketGeoScannerPoll> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketGeoScannerPoll::decode);

  @Override
  public Type<PacketGeoScannerPoll> type() {
    return TYPE;
  }

  public static void encode(PacketGeoScannerPoll msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.knownRevision);
  }

  public static PacketGeoScannerPoll decode(FriendlyByteBuf buf) {
    return new PacketGeoScannerPoll(buf.readBlockPos(), buf.readInt());
  }

  public static void handle(PacketGeoScannerPoll msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof BlockEntityGeoScanner scanner) {
          boolean withScans = scanner.getRevision() != msg.knownRevision();
          ModNetworking.sendToPlayer(player, PacketGeoScannerState.of(scanner, false, withScans));
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
