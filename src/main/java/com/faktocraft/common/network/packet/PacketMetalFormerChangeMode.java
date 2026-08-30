package com.faktocraft.common.network.packet;

import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketMetalFormerChangeMode(BlockPos blockPos) {

  public static void encode(PacketMetalFormerChangeMode msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketMetalFormerChangeMode decode(FriendlyByteBuf buf) {
    return new PacketMetalFormerChangeMode(buf.readBlockPos());
  }

  public static void handle(PacketMetalFormerChangeMode msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.IModeSwitcher modeSwitcher) {
          modeSwitcher.changeMode();
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
