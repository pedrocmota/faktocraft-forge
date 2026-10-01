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

public record PacketMetalFormerChangeMode(BlockPos blockPos) implements CustomPacketPayload {

  public static final Type<PacketMetalFormerChangeMode> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_metal_former_change_mode"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketMetalFormerChangeMode> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketMetalFormerChangeMode::decode);

  @Override
  public Type<PacketMetalFormerChangeMode> type() {
    return TYPE;
  }

  public static void encode(PacketMetalFormerChangeMode msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketMetalFormerChangeMode decode(FriendlyByteBuf buf) {
    return new PacketMetalFormerChangeMode(buf.readBlockPos());
  }

  public static void handle(PacketMetalFormerChangeMode msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.IModeSwitcher modeSwitcher) {
          modeSwitcher.changeMode();
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
