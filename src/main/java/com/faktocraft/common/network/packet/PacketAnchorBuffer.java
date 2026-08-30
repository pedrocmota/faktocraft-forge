package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketAnchorBuffer(BlockPos blockPos, int capacity) {

  public static void encode(PacketAnchorBuffer msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.capacity);
  }

  public static PacketAnchorBuffer decode(FriendlyByteBuf buf) {
    return new PacketAnchorBuffer(buf.readBlockPos(), buf.readInt());
  }

  public static void handle(PacketAnchorBuffer msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof BlockEntityTeleportAnchor anchor) {
          anchor.setBufferCapacity(msg.capacity());
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
