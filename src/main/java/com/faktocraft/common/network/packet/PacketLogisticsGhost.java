package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketLogisticsGhost(List<BlockPos> path, ItemStack stack, int durationTicks, int[] hopTicks) {

  public static void encode(PacketLogisticsGhost msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.path.size());
    for (BlockPos pos : msg.path) {
      buf.writeBlockPos(pos);
    }
    buf.writeItem(msg.stack);
    buf.writeVarInt(msg.durationTicks);
    buf.writeVarInt(msg.hopTicks.length);
    for (int ticks : msg.hopTicks) {
      buf.writeVarInt(ticks);
    }
  }

  public static PacketLogisticsGhost decode(FriendlyByteBuf buf) {
    int size = buf.readVarInt();
    List<BlockPos> path = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      path.add(buf.readBlockPos());
    }
    ItemStack stack = buf.readItem();
    int duration = buf.readVarInt();
    int[] hopTicks = new int[buf.readVarInt()];
    for (int i = 0; i < hopTicks.length; i++) {
      hopTicks[i] = buf.readVarInt();
    }
    return new PacketLogisticsGhost(path, stack, duration, hopTicks);
  }

  public static void handle(PacketLogisticsGhost msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.LogisticsGhosts.add(msg)));
    ctx.get().setPacketHandled(true);
  }

  public static void send(ServerLevel level, List<BlockPos> path, ItemStack stack, int durationTicks,
      int[] hopTicks) {
    if (path.size() < 2 || stack.isEmpty()) {
      return;
    }
    PacketLogisticsGhost msg = new PacketLogisticsGhost(path, stack.copy(), durationTicks, hopTicks);
    BlockPos anchor = path.get(0);
    for (ServerPlayer player : level.players()) {
      if (player.blockPosition().closerThan(anchor, 64)) {
        ModNetworking.sendToPlayer(player, msg);
      }
    }
  }
}
