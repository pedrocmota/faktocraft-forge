package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketTaskHistoryOp(BlockPos blockPos, String dimension, int mode, long id)
    implements CustomPacketPayload {

  public static final Type<PacketTaskHistoryOp> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_task_history_op"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketTaskHistoryOp> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketTaskHistoryOp::decode);

  @Override
  public Type<PacketTaskHistoryOp> type() {
    return TYPE;
  }

  public static final int MODE_DELETE = 0;
  public static final int MODE_CLEAR_DONE = 1;
  public static final int MODE_CANCEL = 2;

  public static void encode(PacketTaskHistoryOp msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeUtf(msg.dimension, 128);
    buf.writeVarInt(msg.mode);
    buf.writeVarLong(msg.id);
  }

  public static PacketTaskHistoryOp decode(FriendlyByteBuf buf) {
    return new PacketTaskHistoryOp(buf.readBlockPos(), buf.readUtf(128), buf.readVarInt(), buf.readVarLong());
  }

  public static void handle(PacketTaskHistoryOp msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null) {
        return;
      }

      if (player.containerMenu instanceof com.faktocraft.common.block.impl.logistics.MenuCoreTasks coreMenu
          && coreMenu.getCorePos().equals(msg.blockPos) && coreMenu.getCore() != null) {
        apply(coreMenu.getCore(), msg, null);
        return;
      }

      if (!(player.containerMenu instanceof com.faktocraft.common.block.impl.logistics.MenuRequestTable menu)
          || !menu.getTablePos().equals(msg.blockPos)) {
        return;
      }
      BlockEntityRequestTable table = menu.getTable();
      if (table == null || table.isRemoved()) {
        return;
      }
      BlockEntityLogisticsController core = table.findCore();
      if (core != null) {
        apply(core, msg, msg.blockPos);
      }
    });
    ctx.setPacketHandled(true);
  }

  private static void apply(BlockEntityLogisticsController core, PacketTaskHistoryOp msg,
      @org.jetbrains.annotations.Nullable BlockPos restrictTable) {
    if (msg.mode == MODE_DELETE) {
      core.getLedger().removeHistory(msg.id, restrictTable);
    } else if (msg.mode == MODE_CLEAR_DONE) {
      core.getLedger().clearHistoryDone(restrictTable);
    } else if (msg.mode == MODE_CANCEL && core.getLevel() != null) {
      core.getLedger().cancelUserJob(core.getLevel(), msg.id, restrictTable);
    }
  }
}
