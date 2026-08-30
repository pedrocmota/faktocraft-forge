package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.logistics.MenuRequestTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketRequestTarget(BlockPos blockPos, ItemStack stack) {

  public static void encode(PacketRequestTarget msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeItem(msg.stack);
  }

  public static PacketRequestTarget decode(FriendlyByteBuf buf) {
    return new PacketRequestTarget(buf.readBlockPos(), buf.readItem());
  }

  public static void handle(PacketRequestTarget msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player != null && player.containerMenu instanceof MenuRequestTable menu
          && msg.blockPos.equals(menu.getTablePos())) {
        menu.setGhostFromPacket(msg.stack);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
