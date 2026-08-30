package com.faktocraft.common.network.packet;

import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketExperience(BlockPos blockPos) {

  public static void encode(PacketExperience msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketExperience decode(FriendlyByteBuf buf) {
    return new PacketExperience(buf.readBlockPos());
  }

  public static void handle(PacketExperience msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IExpCollector expCollector) {
          expCollector.collectExp(player);
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
