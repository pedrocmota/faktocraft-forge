package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketGeoScannerPoll(BlockPos blockPos, int knownRevision) {

  public static void encode(PacketGeoScannerPoll msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.knownRevision);
  }

  public static PacketGeoScannerPoll decode(FriendlyByteBuf buf) {
    return new PacketGeoScannerPoll(buf.readBlockPos(), buf.readInt());
  }

  public static void handle(PacketGeoScannerPoll msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
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
    ctx.get().setPacketHandled(true);
  }
}
