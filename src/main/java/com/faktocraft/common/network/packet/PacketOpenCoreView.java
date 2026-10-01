package com.faktocraft.common.network.packet;

import net.minecraft.world.phys.Vec3;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.MenuCoreTasks;
import com.faktocraft.common.container.FaktocraftMenuProvider;
import com.faktocraft.common.interfaces.block.IHasMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.Block;

public record PacketOpenCoreView(BlockPos blockPos, boolean taskBook) implements CustomPacketPayload {

  public static final Type<PacketOpenCoreView> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_open_core_view"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenCoreView> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketOpenCoreView::decode);

  @Override
  public Type<PacketOpenCoreView> type() {
    return TYPE;
  }

  public static void encode(PacketOpenCoreView msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.taskBook);
  }

  public static PacketOpenCoreView decode(FriendlyByteBuf buf) {
    return new PacketOpenCoreView(buf.readBlockPos(), buf.readBoolean());
  }

  public static void handle(PacketOpenCoreView msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null || !player.level().isLoaded(msg.blockPos)
          || !(player.level().getBlockEntity(msg.blockPos) instanceof BlockEntityLogisticsController)
          || player.distanceToSqr(Vec3.atCenterOf(msg.blockPos)) > 64.0) {
        return;
      }
      Block block = player.level().getBlockState(msg.blockPos).getBlock();
      if (msg.taskBook) {
        player.openMenu(new SimpleMenuProvider(
            (windowId, inventory, p) -> new MenuCoreTasks(windowId, p.level(), msg.blockPos, inventory, p),
            Component.translatable("logistics." + com.faktocraft.Faktocraft.MODID + ".task_book")),
            buf -> buf.writeBlockPos(msg.blockPos));
      } else if (block instanceof IHasMenu hasMenu) {
        player.openMenu(new FaktocraftMenuProvider(hasMenu, player.level(), msg.blockPos, block.getName()),
            buf -> buf.writeBlockPos(msg.blockPos));
      }
    });
    ctx.setPacketHandled(true);
  }
}
