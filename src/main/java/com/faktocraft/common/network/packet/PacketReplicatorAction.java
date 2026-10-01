package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketReplicatorAction(BlockPos blockPos, int action) implements CustomPacketPayload {

  public static final Type<PacketReplicatorAction> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_replicator_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketReplicatorAction> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketReplicatorAction::decode);

  @Override
  public Type<PacketReplicatorAction> type() {
    return TYPE;
  }

  public static void encode(PacketReplicatorAction msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeVarInt(msg.action);
  }

  public static PacketReplicatorAction decode(FriendlyByteBuf buf) {
    return new PacketReplicatorAction(buf.readBlockPos(), buf.readVarInt());
  }

  public static void handle(PacketReplicatorAction msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
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
    ctx.setPacketHandled(true);
  }
}
