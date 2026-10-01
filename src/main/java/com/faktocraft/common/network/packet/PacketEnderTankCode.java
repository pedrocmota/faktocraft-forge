package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.pipe.MenuEnderTank;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketEnderTankCode(BlockPos pos, int code) implements CustomPacketPayload {

  public static final Type<PacketEnderTankCode> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_ender_tank_code"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketEnderTankCode> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketEnderTankCode::decode);

  @Override
  public Type<PacketEnderTankCode> type() {
    return TYPE;
  }

  public static void encode(PacketEnderTankCode msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.pos);
    buf.writeVarInt(msg.code);
  }

  public static PacketEnderTankCode decode(FriendlyByteBuf buf) {
    return new PacketEnderTankCode(buf.readBlockPos(), buf.readVarInt());
  }

  public static void handle(PacketEnderTankCode msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player != null && player.containerMenu instanceof MenuEnderTank menu && menu.getTank() != null
          && menu.getTank().getBlockPos().equals(msg.pos) && menu.stillValid(player)) {
        menu.getTank().setCode(msg.code);
      }
    });
    ctx.setPacketHandled(true);
  }
}
