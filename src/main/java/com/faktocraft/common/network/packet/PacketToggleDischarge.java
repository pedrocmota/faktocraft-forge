package com.faktocraft.common.network.packet;

import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketToggleDischarge(BlockPos blockPos) {

  public static void encode(PacketToggleDischarge msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketToggleDischarge decode(FriendlyByteBuf buf) {
    return new PacketToggleDischarge(buf.readBlockPos());
  }

  public static void handle(PacketToggleDischarge msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IndRebBlockEntity faktocraft && faktocraft.hasBatteryDock()) {
          faktocraft.toggleDischargeMode();
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
