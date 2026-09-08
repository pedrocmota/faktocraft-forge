package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.LogisticsGraph;
import com.faktocraft.common.block.impl.logistics.LogisticsPlanner;
import com.faktocraft.common.block.impl.logistics.TaskLedger;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.faktocraft.common.block.impl.logistics.ItemKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public record PacketReqTableState(BlockPos blockPos, String dimension) {

  private static final int MAX_ENTRIES = 2048;

  public static void encode(PacketReqTableState msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeUtf(msg.dimension, 128);
  }

  public static PacketReqTableState decode(FriendlyByteBuf buf) {
    return new PacketReqTableState(buf.readBlockPos(), buf.readUtf(128));
  }

  public static void handle(PacketReqTableState msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) {
        return;
      }
      if (!(player.containerMenu instanceof com.faktocraft.common.block.impl.logistics.MenuRequestTable menu)
          || !menu.getTablePos().equals(msg.blockPos)) {
        return;
      }
      BlockEntityRequestTable table = menu.getTable();
      Level level = table != null ? table.getLevel() : null;
      if (table == null || table.isRemoved() || level == null) {
        return;
      }
      ModNetworking.sendToPlayer(player, build(level, table));
    });
    ctx.get().setPacketHandled(true);
  }

  private static List<PacketTableState.SubLine> subLines(List<TaskLedger.SubRecord> subs) {
    List<PacketTableState.SubLine> lines = new ArrayList<>(subs.size());
    for (TaskLedger.SubRecord sub : subs) {
      lines.add(new PacketTableState.SubLine(sub.kind(), sub.item(), sub.count(), sub.stateKey(),
          sub.leftoverItem(), sub.leftoverCount(), sub.where(), sub.whereDetail()));
    }
    return lines;
  }

  private static PacketTableState build(Level level, BlockEntityRequestTable table) {
    List<PacketTableState.Entry> entries = new ArrayList<>();
    List<PacketTableState.TaskLine> tasks = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    BlockEntityLogisticsController core = table.findCore();
    if (core == null) {
      errors.add("no_controller");
      return new PacketTableState(table.getBlockPos(), entries, tasks, errors);
    }
    LogisticsGraph graph = core.graph();
    if (graph == null) {
      return new PacketTableState(table.getBlockPos(), entries, tasks, errors);
    }
    if (graph.hasCoreConflict()) {
      errors.add("conflict");
    }
    if (core.getEnergyStorage().energyStored() <= 0) {
      errors.add("no_energy_short");
    }
    BlockEntityLogisticsController.GuiSnapshot snapshot = core.guiSnapshot(level, graph);
    Map<ItemKey, Integer> stock = snapshot.stock();
    List<LogisticsPlanner.CraftDecl> decls = snapshot.decls();
    errors.addAll(snapshot.errors());

    Set<ItemKey> known = snapshot.known();
    Set<ItemKey> producible = snapshot.producible();
    for (Map.Entry<ItemKey, Integer> entry : stock.entrySet()) {
      if (entries.size() >= MAX_ENTRIES) {
        break;
      }
      entries.add(new PacketTableState.Entry(entry.getKey().stack(), entry.getValue(),
          producible.contains(entry.getKey()), ItemStack.EMPTY));
    }
    for (ItemKey item : known) {
      if (entries.size() >= MAX_ENTRIES) {
        break;
      }
      if (!stock.containsKey(item)) {
        entries.add(new PacketTableState.Entry(item.stack(), 0, true, ItemStack.EMPTY));
      }
    }

    Map<ItemKey, java.util.List<LogisticsPlanner.CraftDecl>> declIndex = LogisticsPlanner.index(decls);
    Set<ItemKey> listed = new java.util.HashSet<>();
    for (LogisticsPlanner.CraftDecl decl : decls) {
      if (entries.size() >= MAX_ENTRIES) {
        break;
      }
      ItemKey result = decl.result();
      if (known.contains(result) || !listed.add(result)) {
        continue;
      }
      ItemKey missing = LogisticsPlanner.blockingIngredient(result, declIndex, known);
      entries.add(new PacketTableState.Entry(result.stack(), 0, true, missing.stack()));
    }

    long tablePos = table.getBlockPos().asLong();
    List<TaskLedger.TaskSummary> active = new ArrayList<>(core.pendingSummaries());
    active.addAll(core.getLedger().summaries());
    for (TaskLedger.TaskSummary summary : active) {
      if (tasks.size() >= 32) {
        break;
      }
      if (summary.system() || summary.originPos() != tablePos) {
        continue;
      }
      tasks.add(new PacketTableState.TaskLine(summary.id(), summary.stack(), summary.count(),
          summary.stateKey(), summary.detail(), summary.system(), summary.originPos(),
          summary.originLabel(), subLines(summary.subs())));
    }
    for (TaskLedger.HistoryRecord record : core.getLedger().userHistory()) {
      if (tasks.size() >= 32 + TaskLedger.HISTORY_LIMIT) {
        break;
      }
      if (record.originPos() != tablePos) {
        continue;
      }
      tasks.add(new PacketTableState.TaskLine(record.id(), record.stack(), record.count(),
          record.stateKey(), record.detail(), record.system(), record.originPos(),
          record.originLabel(), subLines(record.subs())));
    }
    return new PacketTableState(table.getBlockPos(), entries, tasks, errors);
  }
}
