package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.MenuCoreTasks;
import com.faktocraft.common.container.IndRebMenuProvider;
import com.faktocraft.common.interfaces.block.IHasMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import java.util.function.Supplier;

public record PacketOpenCoreView(BlockPos blockPos, boolean taskBook) {

  public static void encode(PacketOpenCoreView msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.taskBook);
  }

  public static PacketOpenCoreView decode(FriendlyByteBuf buf) {
    return new PacketOpenCoreView(buf.readBlockPos(), buf.readBoolean());
  }

  public static void handle(PacketOpenCoreView msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null || !player.serverLevel().isLoaded(msg.blockPos)
          || !(player.serverLevel().getBlockEntity(msg.blockPos)
              instanceof BlockEntityLogisticsController)
          || player.distanceToSqr(msg.blockPos.getCenter()) > 64.0) {
        return;
      }
      Block block = player.serverLevel().getBlockState(msg.blockPos).getBlock();
      if (msg.taskBook) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (windowId, inventory, p) -> new MenuCoreTasks(windowId, p.level(), msg.blockPos, inventory, p),
            Component.translatable("logistics." + com.faktocraft.IndReb.MODID + ".task_book")),
            buf -> buf.writeBlockPos(msg.blockPos));
      } else if (block instanceof IHasMenu hasMenu) {
        NetworkHooks.openScreen(player,
            new IndRebMenuProvider(hasMenu, player.serverLevel(), msg.blockPos, block.getName()),
            buf -> buf.writeBlockPos(msg.blockPos));
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
