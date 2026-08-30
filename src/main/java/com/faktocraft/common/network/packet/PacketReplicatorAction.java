package com.faktocraft.common.network.packet;

import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketReplicatorAction(BlockPos blockPos, int action) {

  public static void encode(PacketReplicatorAction msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeVarInt(msg.action);
  }

  public static PacketReplicatorAction decode(FriendlyByteBuf buf) {
    return new PacketReplicatorAction(buf.readBlockPos(), buf.readVarInt());
  }

  public static void handle(PacketReplicatorAction msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.IReplicatorActions replicator) {
          switch (msg.action()) {
            case 0 -> replicator.stopRun();
            case 1 -> replicator.singleRun();
            case 2 -> replicator.repeatRun();
          }
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
