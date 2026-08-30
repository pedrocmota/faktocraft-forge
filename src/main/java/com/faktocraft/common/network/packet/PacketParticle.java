package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketParticle(BlockPos blockPos) {

  public static void encode(PacketParticle msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketParticle decode(FriendlyByteBuf buf) {
    return new PacketParticle(buf.readBlockPos());
  }

  public static void handle(PacketParticle msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleParticle(msg)));
    ctx.get().setPacketHandled(true);
  }

  public static void send(ServerLevel level, BlockPos pos) {
    for (ServerPlayer player : level.players()) {
      if (player.blockPosition().closerThan(pos, 64)) {
        ModNetworking.sendToPlayer(player, new PacketParticle(pos));
      }
    }
  }
}
