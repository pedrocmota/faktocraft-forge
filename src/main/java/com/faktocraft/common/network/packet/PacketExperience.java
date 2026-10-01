package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketExperience(BlockPos blockPos) implements CustomPacketPayload {

  public static final Type<PacketExperience> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_experience"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketExperience> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketExperience::decode);

  @Override
  public Type<PacketExperience> type() {
    return TYPE;
  }

  public static void encode(PacketExperience msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketExperience decode(FriendlyByteBuf buf) {
    return new PacketExperience(buf.readBlockPos());
  }

  public static void handle(PacketExperience msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IExpCollector expCollector) {
          expCollector.collectExp(player);
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
