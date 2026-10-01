package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.BufUtil;
import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public record PacketTableState(BlockPos blockPos, List<Entry> entries, List<TaskLine> tasks, List<String> errors)
    implements CustomPacketPayload {

  public static final Type<PacketTableState> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_table_state"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketTableState> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketTableState::decode);

  @Override
  public Type<PacketTableState> type() {
    return TYPE;
  }

  public record Entry(ItemStack stack, int count, boolean craftableOnly, ItemStack missing) {

    public boolean blocked() {
      return !missing.isEmpty();
    }
  }

  public record TaskLine(long id, ItemStack stack, int count, String stateKey, String detail,
      boolean system, long originPos, String originLabel, int delivered, List<SubLine> subs) {
  }

  public record SubLine(String kind, ItemStack stack, int count, String stateKey,
      ItemStack leftoverStack, int leftoverCount, String where, String whereDetail, int done, int inputsDone,
      int inputsTotal) {
  }

  public static void encode(PacketTableState msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeVarInt(msg.entries.size());
    for (Entry entry : msg.entries) {
      BufUtil.writeItem(buf, entry.stack());
      buf.writeVarInt(entry.count());
      buf.writeBoolean(entry.craftableOnly());
      BufUtil.writeItem(buf, entry.missing());
    }
    buf.writeVarInt(msg.tasks.size());
    for (TaskLine task : msg.tasks) {
      buf.writeVarLong(task.id());
      BufUtil.writeItem(buf, task.stack());
      buf.writeVarInt(task.count());
      buf.writeUtf(task.stateKey(), 64);
      buf.writeUtf(task.detail(), 64);
      buf.writeBoolean(task.system());
      buf.writeLong(task.originPos());
      buf.writeUtf(task.originLabel(), 128);
      buf.writeVarInt(task.delivered());
      buf.writeVarInt(task.subs().size());
      for (SubLine sub : task.subs()) {
        buf.writeUtf(sub.kind(), 16);
        BufUtil.writeItem(buf, sub.stack());
        buf.writeVarInt(sub.count());
        buf.writeUtf(sub.stateKey(), 64);
        BufUtil.writeItem(buf, sub.leftoverStack());
        buf.writeVarInt(sub.leftoverCount());
        buf.writeUtf(sub.where(), 16);
        buf.writeUtf(sub.whereDetail(), 128);
        buf.writeVarInt(sub.done());
        buf.writeVarInt(sub.inputsDone());
        buf.writeVarInt(sub.inputsTotal());
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
      entries.add(new Entry(BufUtil.readItem(buf), buf.readVarInt(), buf.readBoolean(), BufUtil.readItem(buf)));
    }
    int taskCount = buf.readVarInt();
    List<TaskLine> tasks = new ArrayList<>(taskCount);
    for (int i = 0; i < taskCount; i++) {
      long id = buf.readVarLong();
      ItemStack stack = BufUtil.readItem(buf);
      int count = buf.readVarInt();
      String stateKey = buf.readUtf(64);
      String detail = buf.readUtf(64);
      boolean system = buf.readBoolean();
      long originPos = buf.readLong();
      String originLabel = buf.readUtf(128);
      int delivered = buf.readVarInt();
      int subCount = buf.readVarInt();
      List<SubLine> subs = new ArrayList<>(subCount);
      for (int s = 0; s < subCount; s++) {
        subs.add(new SubLine(buf.readUtf(16), BufUtil.readItem(buf), buf.readVarInt(), buf.readUtf(64),
            BufUtil.readItem(buf), buf.readVarInt(), buf.readUtf(16), buf.readUtf(128), buf.readVarInt(),
            buf.readVarInt(), buf.readVarInt()));
      }
      tasks.add(new TaskLine(id, stack, count, stateKey, detail, system, originPos, originLabel, delivered,
          subs));
    }
    int errorCount = buf.readVarInt();
    List<String> errors = new ArrayList<>(errorCount);
    for (int i = 0; i < errorCount; i++) {
      errors.add(buf.readUtf(128));
    }
    return new PacketTableState(pos, entries, tasks, errors);
  }

  public static void handle(PacketTableState msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
