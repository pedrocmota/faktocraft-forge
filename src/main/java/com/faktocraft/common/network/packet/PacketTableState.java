package com.faktocraft.common.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketTableState(BlockPos blockPos, List<Entry> entries, List<TaskLine> tasks, List<String> errors) {

  public record Entry(ItemStack stack, int count, boolean craftableOnly, ItemStack missing) {

    public boolean blocked() {
      return !missing.isEmpty();
    }
  }

  public record TaskLine(long id, ItemStack stack, int count, String stateKey, String detail,
      boolean system, long originPos, String originLabel, List<SubLine> subs) {
  }

  public record SubLine(String kind, ItemStack stack, int count, String stateKey,
      ItemStack leftoverStack, int leftoverCount, String where, String whereDetail) {
  }

  public static void encode(PacketTableState msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeVarInt(msg.entries.size());
    for (Entry entry : msg.entries) {
      buf.writeItem(entry.stack());
      buf.writeVarInt(entry.count());
      buf.writeBoolean(entry.craftableOnly());
      buf.writeItem(entry.missing());
    }
    buf.writeVarInt(msg.tasks.size());
    for (TaskLine task : msg.tasks) {
      buf.writeVarLong(task.id());
      buf.writeItem(task.stack());
      buf.writeVarInt(task.count());
      buf.writeUtf(task.stateKey(), 64);
      buf.writeUtf(task.detail(), 64);
      buf.writeBoolean(task.system());
      buf.writeLong(task.originPos());
      buf.writeUtf(task.originLabel(), 128);
      buf.writeVarInt(task.subs().size());
      for (SubLine sub : task.subs()) {
        buf.writeUtf(sub.kind(), 16);
        buf.writeItem(sub.stack());
        buf.writeVarInt(sub.count());
        buf.writeUtf(sub.stateKey(), 64);
        buf.writeItem(sub.leftoverStack());
        buf.writeVarInt(sub.leftoverCount());
        buf.writeUtf(sub.where(), 16);
        buf.writeUtf(sub.whereDetail(), 128);
      }
    }
    buf.writeVarInt(msg.errors.size());
    for (String error : msg.errors) {
      buf.writeUtf(error, 128);
    }
  }

  public static PacketTableState decode(FriendlyByteBuf buf) {
    BlockPos pos = buf.readBlockPos();
    int entryCount = buf.readVarInt();
    List<Entry> entries = new ArrayList<>(entryCount);
    for (int i = 0; i < entryCount; i++) {
      entries.add(new Entry(buf.readItem(), buf.readVarInt(), buf.readBoolean(), buf.readItem()));
    }
    int taskCount = buf.readVarInt();
    List<TaskLine> tasks = new ArrayList<>(taskCount);
    for (int i = 0; i < taskCount; i++) {
      long id = buf.readVarLong();
      ItemStack stack = buf.readItem();
      int count = buf.readVarInt();
      String stateKey = buf.readUtf(64);
      String detail = buf.readUtf(64);
      boolean system = buf.readBoolean();
      long originPos = buf.readLong();
      String originLabel = buf.readUtf(128);
      int subCount = buf.readVarInt();
      List<SubLine> subs = new ArrayList<>(subCount);
      for (int s = 0; s < subCount; s++) {
        subs.add(new SubLine(buf.readUtf(16), buf.readItem(), buf.readVarInt(), buf.readUtf(64),
            buf.readItem(), buf.readVarInt(), buf.readUtf(16), buf.readUtf(128)));
      }
      tasks.add(new TaskLine(id, stack, count, stateKey, detail, system, originPos, originLabel, subs));
    }
    int errorCount = buf.readVarInt();
    List<String> errors = new ArrayList<>(errorCount);
    for (int i = 0; i < errorCount; i++) {
      errors.add(buf.readUtf(128));
    }
    return new PacketTableState(pos, entries, tasks, errors);
  }

  public static void handle(PacketTableState msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleTableState(msg)));
    ctx.get().setPacketHandled(true);
  }
}
