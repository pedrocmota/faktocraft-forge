package com.faktocraft.common.network.packet;

import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketScannerCleanScan(BlockPos blockPos) {

  public static void encode(PacketScannerCleanScan msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketScannerCleanScan decode(FriendlyByteBuf buf) {
    return new PacketScannerCleanScan(buf.readBlockPos());
  }

  public static void handle(PacketScannerCleanScan msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.IScannerActions scanner) {
          scanner.cleanScan();
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
