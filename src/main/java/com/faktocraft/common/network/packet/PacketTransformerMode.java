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

public record PacketTransformerMode(BlockPos blockPos) implements CustomPacketPayload {

  public static final Type<PacketTransformerMode> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_transformer_mode"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketTransformerMode> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketTransformerMode::decode);

  @Override
  public Type<PacketTransformerMode> type() {
    return TYPE;
  }

  public static void encode(PacketTransformerMode msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketTransformerMode decode(FriendlyByteBuf buf) {
    return new PacketTransformerMode(buf.readBlockPos());
  }

  public static void handle(PacketTransformerMode msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.ITransformerActions transformer) {
          transformer.updateMode();
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
