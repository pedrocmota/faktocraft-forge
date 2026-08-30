package com.faktocraft.common.network.packet;

import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketTransformerMode(BlockPos blockPos) {

  public static void encode(PacketTransformerMode msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketTransformerMode decode(FriendlyByteBuf buf) {
    return new PacketTransformerMode(buf.readBlockPos());
  }

  public static void handle(PacketTransformerMode msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.ITransformerActions transformer) {
          transformer.updateMode();
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
