package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record PacketParticle(BlockPos blockPos) implements CustomPacketPayload {

  public static final Type<PacketParticle> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_particle"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketParticle> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketParticle::decode);

  @Override
  public Type<PacketParticle> type() {
    return TYPE;
  }

  public static void encode(PacketParticle msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketParticle decode(FriendlyByteBuf buf) {
    return new PacketParticle(buf.readBlockPos());
  }

  public static void handle(PacketParticle msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }

  public static void send(ServerLevel level, BlockPos pos) {
    for (ServerPlayer player : level.players()) {
      if (player.blockPosition().closerThan(pos, 64)) {
        ModNetworking.sendToPlayer(player, new PacketParticle(pos));
      }
    }
  }
}
