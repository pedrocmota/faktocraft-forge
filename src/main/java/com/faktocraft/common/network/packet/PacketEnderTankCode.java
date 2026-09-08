package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.pipe.MenuEnderTank;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketEnderTankCode(BlockPos pos, int code) {

  public static void encode(PacketEnderTankCode msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.pos);
    buf.writeVarInt(msg.code);
  }

  public static PacketEnderTankCode decode(FriendlyByteBuf buf) {
    return new PacketEnderTankCode(buf.readBlockPos(), buf.readVarInt());
  }

  public static void handle(PacketEnderTankCode msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player != null && player.containerMenu instanceof MenuEnderTank menu && menu.getTank() != null
          && menu.getTank().getBlockPos().equals(msg.pos) && menu.stillValid(player)) {
        menu.getTank().setCode(msg.code);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
