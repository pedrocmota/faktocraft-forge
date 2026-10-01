package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketToggleDischarge(BlockPos blockPos) implements CustomPacketPayload {

  public static final Type<PacketToggleDischarge> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_toggle_discharge"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketToggleDischarge> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketToggleDischarge::decode);

  @Override
  public Type<PacketToggleDischarge> type() {
    return TYPE;
  }

  public static void encode(PacketToggleDischarge msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketToggleDischarge decode(FriendlyByteBuf buf) {
    return new PacketToggleDischarge(buf.readBlockPos());
  }

  public static void handle(PacketToggleDischarge msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof FaktocraftBlockEntity faktocraft && faktocraft.hasBatteryDock()) {
          faktocraft.toggleDischargeMode();
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
