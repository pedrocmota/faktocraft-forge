package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketTaskHistoryOp(BlockPos blockPos, String dimension, int mode, long id) {

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

  public static void handle(PacketTaskHistoryOp msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) {
        return;
      }

      if (player.containerMenu instanceof com.faktocraft.common.block.impl.logistics.MenuCoreTasks coreMenu
          && coreMenu.getCorePos().equals(msg.blockPos) && coreMenu.getCore() != null) {
        apply(coreMenu.getCore(), msg, null);
        return;
      }

      ServerLevel level = player.server.getLevel(ResourceKey.create(
          net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(msg.dimension)));
      if (level == null || !level.isLoaded(msg.blockPos)
          || !(level.getBlockEntity(msg.blockPos) instanceof BlockEntityRequestTable table)) {
        return;
      }
      if (!(player.containerMenu instanceof
          com.faktocraft.common.block.impl.logistics.MenuRequestTable menu)
          || !menu.getTablePos().equals(msg.blockPos)) {
        return;
      }
      BlockEntityLogisticsController core = table.findCore();
      if (core != null) {
        apply(core, msg, msg.blockPos);
      }
    });
    ctx.get().setPacketHandled(true);
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
