package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketGeoScannerManual(BlockPos blockPos, int chunkX, int chunkZ) {

  public static void encode(PacketGeoScannerManual msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.chunkX);
    buf.writeInt(msg.chunkZ);
  }

  public static PacketGeoScannerManual decode(FriendlyByteBuf buf) {
    return new PacketGeoScannerManual(buf.readBlockPos(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketGeoScannerManual msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
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
    ctx.get().setPacketHandled(true);
  }
}
