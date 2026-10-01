package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.BufUtil;
import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public record PacketLogisticsGhost(List<BlockPos> path, ItemStack stack, int durationTicks, int[] hopTicks)
    implements CustomPacketPayload {

  public static final Type<PacketLogisticsGhost> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_logistics_ghost"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketLogisticsGhost> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketLogisticsGhost::decode);

  @Override
  public Type<PacketLogisticsGhost> type() {
    return TYPE;
  }

  public static void encode(PacketLogisticsGhost msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.path.size());
    for (BlockPos pos : msg.path) {
      buf.writeBlockPos(pos);
    }
    BufUtil.writeItem(buf, msg.stack);
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
    ItemStack stack = BufUtil.readItem(buf);
    int duration = buf.readVarInt();
    int[] hopTicks = new int[buf.readVarInt()];
    for (int i = 0; i < hopTicks.length; i++) {
      hopTicks[i] = buf.readVarInt();
    }
    return new PacketLogisticsGhost(path, stack, duration, hopTicks);
  }

  public static void handle(PacketLogisticsGhost msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
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
