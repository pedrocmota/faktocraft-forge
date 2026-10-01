package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.LogisticsGraph;
import com.faktocraft.common.block.impl.logistics.MenuCoreTasks;
import com.faktocraft.common.block.impl.logistics.TaskLedger;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;

public record PacketCoreTasksReq(BlockPos blockPos) implements CustomPacketPayload {

  public static final Type<PacketCoreTasksReq> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_core_tasks_req"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketCoreTasksReq> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketCoreTasksReq::decode);

  @Override
  public Type<PacketCoreTasksReq> type() {
    return TYPE;
  }

  public static void encode(PacketCoreTasksReq msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
  }

  public static PacketCoreTasksReq decode(FriendlyByteBuf buf) {
    return new PacketCoreTasksReq(buf.readBlockPos());
  }

  public static void handle(PacketCoreTasksReq msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null || !(player.containerMenu instanceof MenuCoreTasks menu)
          || !menu.getCorePos().equals(msg.blockPos)) {
        return;
      }
      BlockEntityLogisticsController core = menu.getCore();
      if (core == null) {
        return;
      }
      List<PacketTableState.TaskLine> tasks = new ArrayList<>();
      List<String> errors = new ArrayList<>();
      LogisticsGraph graph = core.graph();
      if (graph != null && graph.hasCoreConflict()) {
        errors.add("conflict");
      }
      if (core.getEnergyStorage().energyStored() <= 0) {
        errors.add("no_energy_short");
      }
      List<TaskLedger.TaskSummary> active = new ArrayList<>(core.pendingSummaries());
      active.addAll(core.getLedger().summaries());
      for (TaskLedger.TaskSummary summary : active) {
        if (tasks.size() >= 64) {
          break;
        }
        tasks.add(line(summary.id(), summary.stack(), summary.count(), summary.stateKey(), summary.detail(),
            summary.system(), summary.originPos(), summary.originLabel(), summary.delivered(), summary.subs()));
      }
      for (List<TaskLedger.HistoryRecord> history : List.of(core.getLedger().userHistory(),
          core.getLedger().systemHistory())) {
        for (TaskLedger.HistoryRecord record : history) {
          tasks.add(line(record.id(), record.stack(), record.count(), record.stateKey(), record.detail(),
              record.system(), record.originPos(), record.originLabel(), record.delivered(), record.subs()));
        }
      }
      ModNetworking.sendToPlayer(player,
          new PacketTableState(msg.blockPos, List.of(), tasks, errors));
    });
    ctx.setPacketHandled(true);
  }

  private static PacketTableState.TaskLine line(long id, net.minecraft.world.item.ItemStack stack, int count,
      String stateKey, String detail, boolean system, long originPos, String originLabel, int delivered,
      List<TaskLedger.SubRecord> subs) {
    List<PacketTableState.SubLine> lines = new ArrayList<>(subs.size());
    for (TaskLedger.SubRecord sub : subs) {
      lines.add(new PacketTableState.SubLine(sub.kind(), sub.item(), sub.count(), sub.stateKey(),
          sub.leftoverItem(), sub.leftoverCount(), sub.where(), sub.whereDetail(), sub.done(), sub.inputsDone(),
          sub.inputsTotal()));
    }
    return new PacketTableState.TaskLine(id, stack, count, stateKey, detail, system, originPos, originLabel,
        delivered, lines);
  }
}
