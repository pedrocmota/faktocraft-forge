package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketAnchorBuffer(BlockPos blockPos, int capacity) implements CustomPacketPayload {

  public static final Type<PacketAnchorBuffer> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_anchor_buffer"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketAnchorBuffer> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketAnchorBuffer::decode);

  @Override
  public Type<PacketAnchorBuffer> type() {
    return TYPE;
  }

  public static void encode(PacketAnchorBuffer msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.capacity);
  }

  public static PacketAnchorBuffer decode(FriendlyByteBuf buf) {
    return new PacketAnchorBuffer(buf.readBlockPos(), buf.readInt());
  }

  public static void handle(PacketAnchorBuffer msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof BlockEntityTeleportAnchor anchor) {
          anchor.setBufferCapacity(msg.capacity());
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
