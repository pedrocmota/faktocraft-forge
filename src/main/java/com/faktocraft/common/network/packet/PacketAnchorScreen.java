package com.faktocraft.common.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketAnchorScreen(BlockPos blockPos, int current, int min, int max) {

  public static void encode(PacketAnchorScreen msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.current);
    buf.writeInt(msg.min);
    buf.writeInt(msg.max);
  }

  public static PacketAnchorScreen decode(FriendlyByteBuf buf) {
    return new PacketAnchorScreen(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketAnchorScreen msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleAnchorScreen(msg)));
    ctx.get().setPacketHandled(true);
  }
}
