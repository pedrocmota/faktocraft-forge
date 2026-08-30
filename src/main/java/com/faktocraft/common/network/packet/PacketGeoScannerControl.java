package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketGeoScannerControl(BlockPos blockPos, boolean running) {

  public static void encode(PacketGeoScannerControl msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.running);
  }

  public static PacketGeoScannerControl decode(FriendlyByteBuf buf) {
    return new PacketGeoScannerControl(buf.readBlockPos(), buf.readBoolean());
  }

  public static void handle(PacketGeoScannerControl msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
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
    ctx.get().setPacketHandled(true);
  }
}
