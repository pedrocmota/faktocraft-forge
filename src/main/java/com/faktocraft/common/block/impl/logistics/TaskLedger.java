package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.network.packet.PacketLogisticsGhost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TaskLedger {

  private static final int DELIVER_RETRIES = 10;
  private static final int RETRY_INTERVAL = 20;
  public static final int HISTORY_LIMIT = 100;

  public static final class SubRecord {
    String kind = "craft";
    ItemStack item = ItemStack.EMPTY;
    int count;
    String stateKey = "pending";
    ItemStack leftoverItem = ItemStack.EMPTY;
    int leftoverCount;

    String where = "";
    String whereDetail = "";
    long leftoverPos;

    public String kind() {
      return kind;
    }

    public ItemStack item() {
      return item;
    }

    public int count() {
      return count;
    }

    public String stateKey() {
      return stateKey;
    }

    public ItemStack leftoverItem() {
      return leftoverItem;
    }

    public int leftoverCount() {
      return leftoverCount;
    }

    public String where() {
      return where;
    }

    public String whereDetail() {
      return whereDetail;
    }

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putString("kind", kind);
      tag.put("item", item.save(new CompoundTag()));
      tag.putInt("count", count);
      tag.putString("state", stateKey);
      if (leftoverCount > 0) {
        tag.put("leftItem", leftoverItem.save(new CompoundTag()));
        tag.putInt("leftCount", leftoverCount);
        tag.putString("where", where);
        tag.putString("whereDetail", whereDetail);
        tag.putLong("leftPos", leftoverPos);
      }
      return tag;
    }

    static SubRecord load(CompoundTag tag) {
      SubRecord sub = new SubRecord();
      sub.kind = tag.getString("kind");
      sub.item = ItemStack.of(tag.getCompound("item"));
      sub.count = tag.getInt("count");
      sub.stateKey = tag.getString("state");
      if (tag.contains("leftCount")) {
        sub.leftoverItem = ItemStack.of(tag.getCompound("leftItem"));
        sub.leftoverCount = tag.getInt("leftCount");
        sub.where = tag.getString("where");
        sub.whereDetail = tag.getString("whereDetail");
        sub.leftoverPos = tag.getLong("leftPos");
      }
      return sub;
    }
  }

  public static final class HistoryRecord {
    long id;
    ItemStack stack = ItemStack.EMPTY;
    int count;
    String stateKey = "done";
    String detail = "";
    boolean system;
    long originPos;
    String originLabel = "";
    final List<SubRecord> subs = new ArrayList<>();

    public long id() {
      return id;
    }

    public boolean system() {
      return system;
    }

    public long originPos() {
      return originPos;
    }

    public String originLabel() {
      return originLabel;
    }

    public ItemStack stack() {
      return stack;
    }

    public int count() {
      return count;
    }

    public String stateKey() {
      return stateKey;
    }

    public String detail() {
      return detail;
    }

    public List<SubRecord> subs() {
      return subs;
    }

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putLong("id", id);
      tag.put("stack", stack.save(new CompoundTag()));
      tag.putInt("count", count);
      tag.putString("state", stateKey);
      tag.putString("detail", detail);
      tag.putBoolean("system", system);
      tag.putLong("origin", originPos);
      tag.putString("originLabel", originLabel);
      ListTag subsTag = new ListTag();
      for (SubRecord sub : subs) {
        subsTag.add(sub.save());
      }
      tag.put("subs", subsTag);
      return tag;
    }

    static HistoryRecord load(CompoundTag tag) {
      HistoryRecord record = new HistoryRecord();
      record.id = tag.getLong("id");
      record.stack = ItemStack.of(tag.getCompound("stack"));
      record.count = tag.getInt("count");
      record.stateKey = tag.getString("state");
      record.detail = tag.getString("detail");
      record.system = tag.getBoolean("system");
      record.originPos = tag.getLong("origin");
      record.originLabel = tag.getString("originLabel");
      for (Tag element : tag.getList("subs", Tag.TAG_COMPOUND)) {
        record.subs.add(SubRecord.load((CompoundTag) element));
      }
      return record;
    }
  }

  public record TaskSummary(long id, ItemStack stack, int count, String stateKey, String detail,
      boolean system, long originPos, String originLabel, List<SubRecord> subs) {
  }

  public static final class DeliveryTask {
    long id;
    ItemStack stack;
    Endpoint from;
    Endpoint to;
    List<BlockPos> route = List.of();
    int travelTicks = 2;
    int progress;
    int retries;
    int retryCooldown;
    long jobId;
    boolean delivering;
    boolean stockPart;

    boolean leftover;

    long originPos;

    int[] sectionLastIndex = new int[0];

    public int travelTicks() {
      return travelTicks;
    }

    void sliceRoute() {
      Map<Long, Integer> last = new java.util.LinkedHashMap<>();
      for (int i = 0; i < route.size(); i++) {
        BlockPos pos = route.get(i);
        last.put(SectionPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), 0,
            SectionPos.blockToSectionCoord(pos.getZ())), i);
      }
      sectionLastIndex = new int[last.size()];
      int i = 0;
      for (int index : last.values()) {
        sectionLastIndex[i++] = index;
      }
    }

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putLong("id", id);

      tag.put("stack", stack.copyWithCount(Math.min(1, stack.getCount())).save(new CompoundTag()));
      tag.putInt("stackCount", stack.getCount());
      tag.put("from", from.save());
      tag.put("to", to.save());
      ListTag routeTag = new ListTag();
      for (BlockPos pos : route) {
        CompoundTag entry = new CompoundTag();
        entry.putLong("p", pos.asLong());
        routeTag.add(entry);
      }
      tag.put("route", routeTag);
      tag.putInt("travel", travelTicks);
      tag.putInt("progress", progress);
      tag.putInt("retries", retries);
      tag.putLong("job", jobId);
      tag.putBoolean("delivering", delivering);
      tag.putBoolean("stockPart", stockPart);
      tag.putBoolean("leftover", leftover);
      tag.putLong("origin", originPos);
      return tag;
    }

    static DeliveryTask load(CompoundTag tag) {
      DeliveryTask task = new DeliveryTask();
      task.id = tag.getLong("id");
      task.stack = ItemStack.of(tag.getCompound("stack"));
      if (tag.contains("stackCount") && !task.stack.isEmpty()) {
        task.stack.setCount(Math.max(1, tag.getInt("stackCount")));
      }
      task.from = Endpoint.load(tag.getCompound("from"));
      task.to = Endpoint.load(tag.getCompound("to"));
      List<BlockPos> route = new ArrayList<>();
      for (Tag element : tag.getList("route", Tag.TAG_COMPOUND)) {
        route.add(BlockPos.of(((CompoundTag) element).getLong("p")));
      }
      task.route = route;
      task.travelTicks = Math.max(2, tag.getInt("travel"));
      task.progress = tag.getInt("progress");
      task.retries = tag.getInt("retries");
      task.jobId = tag.getLong("job");
      task.delivering = tag.getBoolean("delivering");
      task.stockPart = tag.getBoolean("stockPart");
      task.leftover = tag.getBoolean("leftover");
      task.originPos = tag.getLong("origin");
      task.sliceRoute();
      return task;
    }
  }

  public static final class StepData {
    ItemStack result = ItemStack.EMPTY;
    int resultCount = 1;
    int times = 1;
    int surplus;
    int timeout;
    List<LogisticsPlanner.ItemCount> ingredients = List.of();

    List<LogisticsPlanner.StationChoice> stations = List.of();
    int chosen;
    boolean machine;

    LogisticsPlanner.StationChoice station() {
      return stations.get(chosen < stations.size() ? chosen : 0);
    }

    Endpoint output() {
      return station().output();
    }

    List<Endpoint> ingredientEnds() {
      return station().ingredientEnds();
    }

    BlockPos node() {
      return station().node();
    }

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.put("result", result.save(new CompoundTag()));
      tag.putInt("rc", resultCount);
      tag.putInt("times", times);
      tag.putInt("surplus", surplus);
      tag.putInt("timeout", timeout);
      ListTag ingTag = new ListTag();
      for (LogisticsPlanner.ItemCount ingredient : ingredients) {
        CompoundTag entry = new CompoundTag();
        entry.put("key", ingredient.item().save());
        entry.putInt("count", ingredient.count());
        ingTag.add(entry);
      }
      tag.put("ings", ingTag);
      ListTag stationsTag = new ListTag();
      for (LogisticsPlanner.StationChoice choice : stations) {
        CompoundTag entry = new CompoundTag();
        entry.putLong("node", choice.node().asLong());
        entry.put("out", choice.output().save());
        ListTag endsTag = new ListTag();
        for (Endpoint end : choice.ingredientEnds()) {
          endsTag.add(end.save());
        }
        entry.put("ends", endsTag);
        stationsTag.add(entry);
      }
      tag.put("stations", stationsTag);
      tag.putInt("chosen", chosen);
      tag.putBoolean("machine", machine);
      return tag;
    }

    static StepData load(CompoundTag tag) {
      StepData step = new StepData();
      step.result = ItemStack.of(tag.getCompound("result"));
      step.resultCount = Math.max(1, tag.getInt("rc"));
      step.times = Math.max(1, tag.getInt("times"));
      step.surplus = tag.getInt("surplus");
      step.timeout = tag.getInt("timeout");
      List<LogisticsPlanner.ItemCount> ingredients = new ArrayList<>();
      List<Endpoint> legacyEnds = new ArrayList<>();
      for (Tag element : tag.getList("ings", Tag.TAG_COMPOUND)) {
        CompoundTag entry = (CompoundTag) element;

        ItemKey key = entry.contains("key")
            ? ItemKey.load(entry.getCompound("key"))
            : ItemKey.of(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(
                entry.getString("item"))));
        ingredients.add(new LogisticsPlanner.ItemCount(key, entry.getInt("count")));
        if (entry.contains("end")) {
          legacyEnds.add(Endpoint.load(entry.getCompound("end")));
        }
      }
      step.ingredients = ingredients;
      List<LogisticsPlanner.StationChoice> stations = new ArrayList<>();
      for (Tag element : tag.getList("stations", Tag.TAG_COMPOUND)) {
        CompoundTag entry = (CompoundTag) element;
        List<Endpoint> ends = new ArrayList<>();
        for (Tag endTag : entry.getList("ends", Tag.TAG_COMPOUND)) {
          ends.add(Endpoint.load((CompoundTag) endTag));
        }
        stations.add(new LogisticsPlanner.StationChoice(BlockPos.of(entry.getLong("node")),
            Endpoint.load(entry.getCompound("out")), ends));
      }
      if (stations.isEmpty() && tag.contains("out")) {

        stations.add(new LogisticsPlanner.StationChoice(BlockPos.of(tag.getLong("chassis")),
            Endpoint.load(tag.getCompound("out")), legacyEnds));
      }
      step.stations = stations;
      step.chosen = tag.getInt("chosen");
      step.machine = tag.getBoolean("machine");
      return step;
    }
  }

  public static final class RequestJob {
    long id;
    ItemStack target = ItemStack.EMPTY;
    int quantity;

    boolean system;
    long originPos;
    String originLabel = "";

    int directQuantity;
    Map<ItemKey, Integer> stockToDeliver = new HashMap<>();
    Set<Long> stockPending = new HashSet<>();
    Endpoint dest;
    BlockPos destNode = BlockPos.ZERO;
    Map<ItemKey, Integer> withdrawalsRemaining = new HashMap<>();
    List<StepData> steps = new ArrayList<>();
    int stage;
    boolean producing;
    Map<ItemKey, Integer> toDeliver = new HashMap<>();
    Set<Long> pending = new HashSet<>();
    int outputBaseline;
    int produceWait;

    int stallWait;
    long stallMark;

    List<BlockPos> stations = new ArrayList<>();
    @Nullable
    String error;
    int lingerTicks;
    boolean finalStage;

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putLong("id", id);
      tag.put("target", target.save(new CompoundTag()));
      tag.putInt("qty", quantity);
      tag.putBoolean("system", system);
      tag.putLong("origin", originPos);
      tag.putString("originLabel", originLabel);
      tag.putInt("direct", directQuantity);
      tag.put("stockDeliver", saveKeyMap(stockToDeliver));
      tag.put("dest", dest.save());
      tag.putLong("destNode", destNode.asLong());
      tag.put("withdraw", saveKeyMap(withdrawalsRemaining));
      ListTag stepsTag = new ListTag();
      for (StepData step : steps) {
        stepsTag.add(step.save());
      }
      tag.put("steps", stepsTag);
      tag.putInt("stage", stage);
      tag.putBoolean("producing", producing);
      tag.put("toDeliver", saveKeyMap(toDeliver));
      tag.putInt("baseline", outputBaseline);
      tag.putInt("wait", produceWait);
      long[] held = new long[stations.size()];
      for (int i = 0; i < held.length; i++) {
        held[i] = stations.get(i).asLong();
      }
      tag.putLongArray("stations", held);
      if (error != null) {
        tag.putString("error", error);
      }
      tag.putBoolean("final", finalStage);
      return tag;
    }

    static RequestJob load(CompoundTag tag) {
      RequestJob job = new RequestJob();
      job.id = tag.getLong("id");
      job.target = ItemStack.of(tag.getCompound("target"));
      job.quantity = tag.getInt("qty");
      job.system = tag.getBoolean("system");
      job.originPos = tag.getLong("origin");
      job.originLabel = tag.getString("originLabel");
      job.directQuantity = tag.getInt("direct");
      job.stockToDeliver = loadKeyMap(tag, "stockDeliver");
      job.dest = Endpoint.load(tag.getCompound("dest"));
      job.destNode = BlockPos.of(tag.getLong("destNode"));
      job.withdrawalsRemaining = loadKeyMap(tag, "withdraw");
      for (Tag element : tag.getList("steps", Tag.TAG_COMPOUND)) {
        job.steps.add(StepData.load((CompoundTag) element));
      }
      job.stage = tag.getInt("stage");
      job.producing = tag.getBoolean("producing");
      job.toDeliver = loadKeyMap(tag, "toDeliver");
      job.outputBaseline = tag.getInt("baseline");
      job.produceWait = tag.getInt("wait");
      for (long packed : tag.getLongArray("stations")) {
        job.stations.add(BlockPos.of(packed));
      }
      job.error = tag.contains("error") ? tag.getString("error") : null;
      job.finalStage = tag.getBoolean("final");
      return job;
    }
  }

  private static ListTag saveKeyMap(Map<ItemKey, Integer> map) {
    ListTag list = new ListTag();
    map.forEach((key, count) -> {
      CompoundTag entry = new CompoundTag();
      entry.put("k", key.save());
      entry.putInt("n", count);
      list.add(entry);
    });
    return list;
  }

  private static Map<ItemKey, Integer> loadKeyMap(CompoundTag owner, String name) {
    Map<ItemKey, Integer> map = new HashMap<>();
    if (owner.contains(name, Tag.TAG_LIST)) {
      for (Tag element : owner.getList(name, Tag.TAG_COMPOUND)) {
        CompoundTag entry = (CompoundTag) element;
        ItemKey key = ItemKey.load(entry.getCompound("k"));
        if (!key.isEmpty()) {
          map.put(key, entry.getInt("n"));
        }
      }
      return map;
    }
    CompoundTag legacy = owner.getCompound(name);
    for (String id : legacy.getAllKeys()) {
      ItemKey key = ItemKey.of(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));
      if (!key.isEmpty()) {
        map.put(key, legacy.getInt(id));
      }
    }
    return map;
  }

  private final BlockEntityLogisticsController core;
  private long nextId = 1;
  private final List<DeliveryTask> deliveries = new ArrayList<>();
  private final List<RequestJob> jobs = new ArrayList<>();
  private final Map<ItemKey, Integer> reserved = new HashMap<>();

  private final List<HistoryRecord> userHistory = new ArrayList<>();
  private final List<HistoryRecord> systemHistory = new ArrayList<>();

  public List<HistoryRecord> userHistory() {
    return userHistory;
  }

  public List<HistoryRecord> systemHistory() {
    return systemHistory;
  }

  private List<SubRecord> subRecordsFor(RequestJob job) {
    List<SubRecord> subs = new ArrayList<>();
    if (job.directQuantity > 0) {
      SubRecord sub = new SubRecord();
      sub.kind = "deliver";
      sub.item = job.target.copy();
      sub.count = job.directQuantity;
      sub.stateKey = job.stockToDeliver.isEmpty() && job.stockPending.isEmpty() ? "done" : "delivering";
      subs.add(sub);
    }
    for (int i = 0; i < job.steps.size(); i++) {
      StepData step = job.steps.get(i);
      SubRecord sub = new SubRecord();
      sub.kind = "craft";
      sub.item = step.result.copy();
      sub.count = step.times * step.resultCount;
      sub.stateKey = job.stage > i ? "done"
          : job.stage != i ? "pending"
          : job.stations.isEmpty() && !job.steps.isEmpty() ? "waiting"
          : job.producing ? "producing" : "collecting";
      if (step.surplus > 0 && job.stage > i) {
        sub.leftoverItem = step.result.copy();
        sub.leftoverCount = step.surplus;
        sub.where = step.machine ? "machine" : "assembly";
        sub.whereDetail = step.machine ? posText(step.output().pos()) : "";
        sub.leftoverPos = step.output().pos().asLong();
      }
      subs.add(sub);
    }
    return subs;
  }

  private void addHistory(RequestJob job, String stateKey, String detail) {
    HistoryRecord record = newRecord(job.target, job.quantity, stateKey, detail);
    record.id = job.id;
    record.system = job.system;
    record.originPos = job.originPos;
    record.originLabel = job.originLabel;
    record.subs.addAll(subRecordsFor(job));

    if (stateKey.startsWith("error.")) {
      String terminal = "error.cancelled".equals(stateKey) ? "cancelled" : "failed";
      for (SubRecord sub : record.subs) {
        if (!"done".equals(sub.stateKey)) {
          sub.stateKey = terminal;
        }
      }
    }
    pushHistory(record);
  }

  public void addRecord(ItemStack stack, int count, String stateKey, String detail, BlockPos origin,
      String originLabel) {
    HistoryRecord record = newRecord(stack, count, stateKey, detail);
    record.originPos = origin.asLong();
    record.originLabel = originLabel;
    pushHistory(record);
  }

  private HistoryRecord newRecord(ItemStack stack, int count, String stateKey, String detail) {
    HistoryRecord record = new HistoryRecord();
    record.id = nextId();
    record.stack = stack.copy();
    record.count = count;
    record.stateKey = stateKey;
    record.detail = detail;
    return record;
  }

  private void pushHistory(HistoryRecord record) {
    List<HistoryRecord> list = record.system ? systemHistory : userHistory;
    list.add(0, record);
    while (list.size() > HISTORY_LIMIT) {
      list.remove(list.size() - 1);
    }
    core.setChanged();
  }

  public void noteLeftoverShipped(BlockPos assemblyPos, ItemKey item, String destDetail) {
    long posLong = assemblyPos.asLong();
    for (List<HistoryRecord> list : List.of(userHistory, systemHistory)) {
      for (HistoryRecord record : list) {
        for (SubRecord sub : record.subs) {
          if (sub.leftoverPos == posLong && "assembly".equals(sub.where)
              && item.matches(sub.leftoverItem)) {
            sub.where = "sink";
            sub.whereDetail = destDetail;
            core.setChanged();
            return;
          }
        }
      }
    }
  }

  public void removeHistory(long id, @Nullable BlockPos restrictTable) {
    boolean removed = userHistory.removeIf(record -> record.id == id
        && (restrictTable == null || record.originPos == restrictTable.asLong()));
    if (restrictTable == null) {
      removed |= systemHistory.removeIf(record -> record.id == id);
    }
    if (removed) {
      core.setChanged();
    }
  }

  public void clearHistoryDone(@Nullable BlockPos restrictTable) {
    boolean removed = userHistory.removeIf(record -> "done".equals(record.stateKey)
        && (restrictTable == null || record.originPos == restrictTable.asLong()));
    if (restrictTable == null) {
      removed |= systemHistory.removeIf(record -> "done".equals(record.stateKey));
    }
    if (removed) {
      core.setChanged();
    }
  }

  public TaskLedger(BlockEntityLogisticsController core) {
    this.core = core;
  }

  public long nextId() {
    return nextId++;
  }

  public int reservedFor(ItemKey item) {
    return reserved.getOrDefault(item, 0);
  }

  public void reserve(Map<ItemKey, Integer> amounts) {
    amounts.forEach((item, count) -> reserved.merge(item, count, Integer::sum));
  }

  private void unreserve(ItemKey item, int count) {
    reserved.merge(item, -count, Integer::sum);
    if (reserved.getOrDefault(item, 0) <= 0) {
      reserved.remove(item);
    }
  }

  public boolean outputInUse(BlockPos assemblyPos) {
    for (RequestJob job : jobs) {
      if (job.error != null || job.stations.isEmpty()) {
        continue;
      }
      for (StepData step : job.steps) {
        if (step.output().pos().equals(assemblyPos)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean hasActiveJobFor(Endpoint dest) {
    for (RequestJob job : jobs) {
      if (job.error == null && job.dest.equals(dest)) {
        return true;
      }
    }
    return false;
  }

  public void addJob(RequestJob job) {
    jobs.add(job);
    core.setChanged();
  }

  public DeliveryTask createDelivery(ItemStack stack, Endpoint from, Endpoint to, List<BlockPos> route, long jobId) {
    DeliveryTask task = new DeliveryTask();
    task.id = nextId();
    task.stack = stack;
    task.from = from;
    task.to = to;
    task.route = route;

    int ticksPerPipe = Math.max(1, ModConfig.server().logistics_ticks_per_pipe);
    int goldTicks = Math.max(1, ticksPerPipe / 2);
    int travel = 0;
    Level level = core.getLevel();
    int[] hopTicks = new int[Math.max(0, route.size() - 1)];
    for (int i = 1; i < route.size(); i++) {
      boolean gold = level != null && level.isLoaded(route.get(i))
          && level.getBlockState(route.get(i))
              .getBlock() instanceof com.faktocraft.common.block.impl.logistics.BlockGoldPipe;
      hopTicks[i - 1] = gold ? goldTicks : ticksPerPipe;
      travel += hopTicks[i - 1];
    }
    task.travelTicks = Math.max(2, Math.max(travel, ticksPerPipe));
    task.jobId = jobId;
    task.sliceRoute();
    deliveries.add(task);
    if (core.getLevel() instanceof ServerLevel serverLevel && route.size() >= 2) {
      PacketLogisticsGhost.send(serverLevel, route, stack, task.travelTicks, hopTicks);
    }
    core.setChanged();
    return task;
  }

  public void tick(Level level, LogisticsGraph graph) {
    tickDeliveries(level, graph);
    tickJobs(level, graph);
  }

  private boolean remainingRouteLoaded(Level level, DeliveryTask task) {
    if (task.route.isEmpty()) {
      return true;
    }
    int index = Math.min(task.route.size() - 1, task.progress * task.route.size() / task.travelTicks);
    for (int lastIndex : task.sectionLastIndex) {
      if (lastIndex >= index && !level.isLoaded(task.route.get(lastIndex))) {
        return false;
      }
    }
    return true;
  }

  private void tickDeliveries(Level level, LogisticsGraph graph) {
    Iterator<DeliveryTask> iterator = deliveries.iterator();
    List<DeliveryTask> finished = new ArrayList<>();
    while (iterator.hasNext()) {
      DeliveryTask task = iterator.next();
      if (!task.delivering) {

        if (!remainingRouteLoaded(level, task)) {
          continue;
        }
        if (++task.progress < task.travelTicks) {
          continue;
        }
        task.delivering = true;
        task.retryCooldown = 0;
      }
      if (--task.retryCooldown > 0) {
        continue;
      }
      task.retryCooldown = RETRY_INTERVAL;
      if (!task.to.isLoaded(level)) {
        continue;
      }
      task.stack = task.to.insert(level, task.stack, false);
      if (task.stack.isEmpty()) {
        iterator.remove();
        finished.add(task);
        continue;
      }
      if (++task.retries < DELIVER_RETRIES) {
        continue;
      }

      if (task.from.isLoaded(level)) {
        task.stack = task.from.insert(level, task.stack, false);
      }
      if (!task.stack.isEmpty()) {
        BlockPos drop = task.to.pos();
        Containers.dropItemStack(level, drop.getX() + 0.5, drop.getY() + 1, drop.getZ() + 0.5, task.stack);
        task.stack = ItemStack.EMPTY;
      }
      iterator.remove();
      finished.add(task);
    }
    if (!finished.isEmpty()) {
      for (DeliveryTask task : finished) {
        if (task.jobId != 0) {
          for (RequestJob job : jobs) {
            if (job.id == task.jobId) {
              job.pending.remove(task.id);
              job.stockPending.remove(task.id);
            }
          }
        }
      }
      core.setChanged();
    }
  }

  private void tickJobs(Level level, LogisticsGraph graph) {
    Iterator<RequestJob> iterator = jobs.iterator();
    while (iterator.hasNext()) {
      RequestJob job = iterator.next();
      tickJob(level, graph, job);
      if (job.error != null) {
        String[] parts = job.error.split(":", 2);
        addHistory(job, "error." + parts[0], parts.length > 1 ? parts[1] : "");
        iterator.remove();
        core.setChanged();
        continue;
      }
      if (job.finalStage && job.toDeliver.isEmpty() && job.pending.isEmpty()
          && job.stockToDeliver.isEmpty() && job.stockPending.isEmpty()) {
        releaseReservations(job);
        addHistory(job, "done", "");
        iterator.remove();
        core.setChanged();
      }
    }
  }

  public boolean cancelUserJob(Level level, long jobId, @Nullable BlockPos restrictOrigin) {
    RequestJob job = null;
    for (RequestJob candidate : jobs) {
      if (candidate.id == jobId) {
        job = candidate;
        break;
      }
    }
    if (job == null || job.system || job.error != null) {
      return false;
    }
    if (restrictOrigin != null && job.originPos != restrictOrigin.asLong()) {
      return false;
    }
    returnInFlight(level, job);
    recoverStations(level, job);
    failJob(job, "cancelled");
    return true;
  }

  private void returnInFlight(Level level, RequestJob job) {
    Iterator<DeliveryTask> iterator = deliveries.iterator();
    while (iterator.hasNext()) {
      DeliveryTask task = iterator.next();
      if (task.jobId != job.id) {
        continue;
      }
      if (task.from.isLoaded(level)) {
        task.stack = task.from.insert(level, task.stack, false);
      }
      if (!task.stack.isEmpty()) {
        BlockPos drop = task.from.pos();
        Containers.dropItemStack(level, drop.getX() + 0.5, drop.getY() + 1, drop.getZ() + 0.5, task.stack);
        task.stack = ItemStack.EMPTY;
      }
      iterator.remove();
    }
  }

  private void recoverStations(Level level, RequestJob job) {
    LogisticsGraph graph = core.graph();
    if (graph == null || job.stations.isEmpty()) {
      return;
    }
    List<BlockEntityChassis.SinkCandidate> sinks = BlockEntityChassis.sinkCandidates(level, graph, null);
    if (sinks.isEmpty()) {
      return;
    }
    for (int s = 0; s <= Math.min(job.stage, job.steps.size() - 1); s++) {
      StepData step = job.steps.get(s);
      if (s == job.stage && !job.finalStage) {
        recoverIngredients(level, graph, sinks, job, step);
      }

      if (!step.machine) {
        continue;
      }
      ItemKey resultKey = ItemKey.of(step.result);
      int cap = s == job.stage
          ? (job.producing ? step.output().count(level, resultKey) - job.outputBaseline : 0)
          : step.times * step.resultCount;
      if (cap > 0) {
        shipToSinks(level, graph, sinks, step.output(), step.node(), resultKey, cap);
      }
    }
  }

  private void recoverIngredients(Level level, LogisticsGraph graph,
      List<BlockEntityChassis.SinkCandidate> sinks, RequestJob job, StepData step) {
    Map<ItemKey, Integer> delivered = new HashMap<>();
    for (LogisticsPlanner.ItemCount ingredient : step.ingredients) {
      delivered.merge(ingredient.item(), ingredient.count(), Integer::sum);
    }
    job.toDeliver.forEach((item, remaining) -> delivered.merge(item, -remaining, Integer::sum));
    for (int i = 0; i < step.ingredients.size(); i++) {
      ItemKey item = step.ingredients.get(i).item();
      int budget = delivered.getOrDefault(item, 0);
      if (budget <= 0) {
        continue;
      }
      int shipped = shipToSinks(level, graph, sinks, step.ingredientEnds().get(i), step.node(), item, budget);
      delivered.merge(item, -shipped, Integer::sum);
    }
  }

  private int shipToSinks(Level level, LogisticsGraph graph, List<BlockEntityChassis.SinkCandidate> sinks,
      Endpoint source, BlockPos node, ItemKey item, int amount) {
    int perItem = Math.max(0, ModConfig.server().logistics_energy_per_item);
    int shipped = 0;
    while (shipped < amount) {
      int take = Math.min(amount - shipped, item.maxStackSize());
      if (perItem > 0) {
        take = Math.min(take, core.getEnergyStorage().energyStored() / perItem);
      }
      if (take <= 0) {
        break;
      }
      BlockEntityChassis.SinkTarget sink = BlockEntityChassis.findSink(level, sinks,
          item.stack(take));
      if (sink == null) {
        break;
      }
      int taken = source.extract(level, item, take, false);
      if (taken <= 0) {
        break;
      }
      core.consumeEnergy(taken * perItem);
      DeliveryTask delivery = createDelivery(item.stack(taken), source,
          sink.endpoint(), graph.route(node, sink.nodePos()), 0);
      delivery.leftover = true;
      delivery.originPos = node.asLong();
      shipped += taken;
    }
    return shipped;
  }

  private void releaseReservations(RequestJob job) {
    job.withdrawalsRemaining.forEach(this::unreserve);
    job.withdrawalsRemaining.clear();
    job.stations.clear();
  }

  private boolean claimStations(RequestJob job) {
    if (!job.stations.isEmpty() || job.steps.isEmpty()) {
      return true;
    }

    List<BlockPos> busy = new ArrayList<>();
    for (RequestJob other : jobs) {
      if (other != job) {
        busy.addAll(other.stations);
      }
    }

    int[] picks = new int[job.steps.size()];
    List<BlockPos> mine = new ArrayList<>();
    for (int i = 0; i < job.steps.size(); i++) {
      StepData step = job.steps.get(i);
      int pick = -1;
      for (int option = 0; option < step.stations.size(); option++) {
        BlockPos pos = step.stations.get(option).output().pos();
        if (!busy.contains(pos)) {
          pick = option;
          break;
        }
      }
      if (pick < 0) {
        return false;
      }
      picks[i] = pick;
      BlockPos pos = step.stations.get(pick).output().pos();
      if (!mine.contains(pos)) {
        mine.add(pos);
      }
    }
    for (int i = 0; i < picks.length; i++) {
      job.steps.get(i).chosen = picks[i];
    }
    job.stations.addAll(mine);
    core.setChanged();
    return true;
  }

  public long stationHolder(BlockPos station) {
    for (RequestJob job : jobs) {
      if (job.stations.contains(station)) {
        return job.id;
      }
    }
    return 0;
  }

  private void failJob(RequestJob job, String error) {
    job.error = error;
    releaseReservations(job);
    core.setChanged();
  }

  private static final int STALL_TIMEOUT_TICKS = 1200;

  private boolean tickStallWatchdog(RequestJob job) {
    long mark = job.stage * 31L + (job.finalStage ? 1 : 0);
    mark = mark * 31 + job.pending.size();
    mark = mark * 31 + job.stockPending.size();
    mark = mark * 31 + job.toDeliver.size();
    mark = mark * 31 + job.stockToDeliver.size();
    for (int count : job.toDeliver.values()) {
      mark += count;
    }
    for (int count : job.stockToDeliver.values()) {
      mark += count;
    }
    if (mark != job.stallMark || job.stallWait <= 0) {
      job.stallMark = mark;
      job.stallWait = STALL_TIMEOUT_TICKS;
      return true;
    }
    if (--job.stallWait <= 0) {
      failJob(job, "timeout:" + posText(job.dest.pos()));
      return false;
    }
    return true;
  }

  private void tickJob(Level level, LogisticsGraph graph, RequestJob job) {

    if (!job.stockToDeliver.isEmpty()) {
      supply(level, graph, job, job.stockToDeliver, null, job.dest, job.destNode, true);
    }

    if (!job.finalStage && !claimStations(job)) {
      return;
    }
    if (!job.finalStage && job.stage >= job.steps.size()) {
      job.finalStage = true;
      int crafted = job.quantity - job.directQuantity;
      job.toDeliver = crafted > 0
          ? new HashMap<>(Map.of(ItemKey.of(job.target), crafted))
          : new HashMap<>();
      job.producing = false;
    }
    if (!job.producing && !tickStallWatchdog(job)) {
      return;
    }
    if (job.finalStage) {
      supply(level, graph, job, job.toDeliver, null, job.dest, job.destNode, false);
      return;
    }
    StepData step = job.steps.get(job.stage);
    if (!job.producing) {

      if (!step.result.isEmpty() && job.outputBaseline == Integer.MIN_VALUE
          && step.output().isLoaded(level)) {
        job.outputBaseline = step.output().count(level, ItemKey.of(step.result));
      }
      if (job.toDeliver.isEmpty() && job.pending.isEmpty() && !step.result.isEmpty()) {

        job.producing = true;
        if (job.outputBaseline == Integer.MIN_VALUE) {
          job.outputBaseline = step.output().count(level, ItemKey.of(step.result));
        }

        job.produceWait = step.timeout > 0 ? step.timeout
            : Math.max(100, ModConfig.server().logistics_machine_timeout);
        core.setChanged();
        return;
      }
      supplyStep(level, graph, job, step);
      return;
    }
    if (!step.output().isLoaded(level)) {
      return;
    }
    int produced = step.output().count(level, ItemKey.of(step.result)) - job.outputBaseline;
    if (produced >= step.times * step.resultCount) {
      job.stage++;
      job.producing = false;
      startStage(level, job);
      core.setChanged();
      return;
    }
    if (--job.produceWait <= 0) {
      failJob(job, "timeout:" + posText(step.output().pos()));
    }
  }

  private static String posText(BlockPos pos) {
    return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
  }

  void startStage(Level level, RequestJob job) {
    if (job.stage >= job.steps.size()) {
      return;
    }
    StepData step = job.steps.get(job.stage);

    Map<ItemKey, Integer> needs = new HashMap<>();
    for (LogisticsPlanner.ItemCount ingredient : step.ingredients) {
      needs.merge(ingredient.item(), ingredient.count(), Integer::sum);
    }
    job.toDeliver = needs;
    job.producing = false;

    job.outputBaseline = Integer.MIN_VALUE;
  }

  private void supplyStep(Level level, LogisticsGraph graph, RequestJob job, StepData step) {
    if (job.toDeliver.isEmpty()) {
      return;
    }

    for (int i = 0; i < step.ingredients.size(); i++) {
      LogisticsPlanner.ItemCount ingredient = step.ingredients.get(i);
      ItemKey item = ingredient.item();
      int remaining = job.toDeliver.getOrDefault(item, 0);
      if (remaining <= 0) {
        continue;
      }
      Endpoint dest = step.ingredientEnds().get(i);
      supply(level, graph, job, job.toDeliver, item, dest, step.node(), false);
    }
  }

  private void supply(Level level, LogisticsGraph graph, RequestJob job, Map<ItemKey, Integer> needs,
      @Nullable ItemKey onlyItem, Endpoint dest, BlockPos destNode, boolean stockPart) {
    List<ItemKey> items = new ArrayList<>(needs.keySet());
    for (ItemKey item : items) {
      if (onlyItem != null && !item.equals(onlyItem)) {
        continue;
      }
      int remaining = needs.getOrDefault(item, 0);
      if (remaining <= 0) {
        needs.remove(item);
        continue;
      }

      if (!stockPart) {
        for (int s = 0; s < job.stage && remaining > 0; s++) {
          StepData earlier = job.steps.get(s);
          remaining = pullFrom(level, graph, job, earlier.output(), earlier.node(), item, remaining, dest,
              destNode, false);
        }
      }
      if (remaining > 0) {
        remaining = pullFromProviders(level, graph, job, item, remaining, dest, destNode, stockPart);
      }
      if (remaining <= 0) {
        needs.remove(item);
      } else {
        needs.put(item, remaining);
      }
    }
  }

  private int pullFrom(Level level, LogisticsGraph graph, RequestJob job, Endpoint source, BlockPos sourceNode,
      ItemKey item, int amount, Endpoint dest, BlockPos destNode, boolean stockPart) {
    if (!source.isLoaded(level)) {
      return amount;
    }
    int available = source.extract(level, item, amount, true);
    if (available <= 0) {
      return amount;
    }
    int taken = source.extract(level, item, available, false);
    if (taken <= 0) {
      return amount;
    }
    List<BlockPos> route = graph.route(sourceNode, destNode);
    DeliveryTask task = createDelivery(item.stack(taken), source, dest, route, job.id);
    task.stockPart = stockPart;
    (stockPart ? job.stockPending : job.pending).add(task.id);
    return amount - taken;
  }

  private int pullFromProviders(Level level, LogisticsGraph graph, RequestJob job, ItemKey item, int amount,
      Endpoint dest, BlockPos destNode, boolean stockPart) {
    int allowed = Math.min(amount, job.withdrawalsRemaining.getOrDefault(item, 0));
    if (allowed <= 0) {
      return amount;
    }
    int pulled = 0;
    List<BlockEntityChassis.ProviderRef> providers = BlockEntityChassis.providers(level, graph);

    providers.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
    for (BlockEntityChassis.ProviderRef provider : providers) {
      if (pulled >= allowed) {
        break;
      }
      int available = provider.availableOf(level, item);
      if (available <= 0) {
        continue;
      }
      int take = Math.min(available, allowed - pulled);
      Endpoint source = Endpoint.inventory(provider.inventoryPos());
      int taken = source.extract(level, item, take, false);
      if (taken <= 0) {
        continue;
      }
      List<BlockPos> route = graph.route(provider.chassisPos(), destNode);
      DeliveryTask task = createDelivery(item.stack(taken), source, dest, route, job.id);
      task.stockPart = stockPart;
      (stockPart ? job.stockPending : job.pending).add(task.id);
      pulled += taken;
    }
    if (pulled > 0) {
      job.withdrawalsRemaining.merge(item, -pulled, Integer::sum);
      if (job.withdrawalsRemaining.getOrDefault(item, 0) <= 0) {
        job.withdrawalsRemaining.remove(item);
      }
      unreserve(item, pulled);
    }
    return amount - pulled;
  }

  public List<TaskSummary> summaries() {
    List<TaskSummary> result = new ArrayList<>();
    for (RequestJob job : jobs) {
      String state;
      String detail = "";
      if (job.finalStage) {
        state = "delivering";
      } else if (job.producing) {
        state = "producing";
        detail = (job.stage + 1) + "/" + job.steps.size();
      } else {
        state = "collecting";
        detail = job.steps.isEmpty() ? "" : (job.stage + 1) + "/" + job.steps.size();
      }
      result.add(new TaskSummary(job.id, job.target, job.quantity, state, detail,
          job.system, job.originPos, job.originLabel, subRecordsFor(job)));
    }
    for (DeliveryTask task : deliveries) {
      if (task.jobId == 0 && !task.leftover) {
        result.add(new TaskSummary(0, task.stack, task.stack.getCount(),
            task.delivering ? "delivering" : "traveling", "", true, task.originPos, "", List.of()));
      }
    }
    return result;
  }

  public int activeJobCount() {
    int count = 0;
    for (RequestJob job : jobs) {
      if (job.error == null) {
        count++;
      }
    }
    return count;
  }

  public void dropCustody(Level level, BlockPos pos) {
    for (DeliveryTask task : deliveries) {
      if (!task.stack.isEmpty()) {
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, task.stack);
      }
    }
    deliveries.clear();
    jobs.clear();
    reserved.clear();
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putLong("nextId", nextId);
    ListTag deliveriesTag = new ListTag();
    for (DeliveryTask task : deliveries) {
      deliveriesTag.add(task.save());
    }
    tag.put("deliveries", deliveriesTag);
    ListTag jobsTag = new ListTag();
    for (RequestJob job : jobs) {
      jobsTag.add(job.save());
    }
    tag.put("jobs", jobsTag);
    tag.put("reserved", saveKeyMap(reserved));
    ListTag userTag = new ListTag();
    for (HistoryRecord record : userHistory) {
      userTag.add(record.save());
    }
    tag.put("historyUser", userTag);
    ListTag systemTag = new ListTag();
    for (HistoryRecord record : systemHistory) {
      systemTag.add(record.save());
    }
    tag.put("historySystem", systemTag);
    return tag;
  }

  public void load(CompoundTag tag) {
    deliveries.clear();
    jobs.clear();
    reserved.clear();
    nextId = Math.max(1, tag.getLong("nextId"));
    for (Tag element : tag.getList("deliveries", Tag.TAG_COMPOUND)) {
      DeliveryTask task = DeliveryTask.load((CompoundTag) element);
      if (!task.stack.isEmpty()) {
        deliveries.add(task);
      }
    }
    for (Tag element : tag.getList("jobs", Tag.TAG_COMPOUND)) {
      jobs.add(RequestJob.load((CompoundTag) element));
    }
    reserved.putAll(loadKeyMap(tag, "reserved"));
    userHistory.clear();
    systemHistory.clear();

    for (String key : new String[] { "historyUser", "historySystem", "history" }) {
      for (Tag element : tag.getList(key, Tag.TAG_COMPOUND)) {
        HistoryRecord record = HistoryRecord.load((CompoundTag) element);
        List<HistoryRecord> list = record.system ? systemHistory : userHistory;
        if (!record.stack.isEmpty() && list.size() < HISTORY_LIMIT) {
          if (record.id == 0) {
            record.id = nextId();
          }
          list.add(record);
        }
      }
    }

    for (RequestJob job : jobs) {
      for (DeliveryTask task : deliveries) {
        if (task.jobId == job.id) {
          (task.stockPart ? job.stockPending : job.pending).add(task.id);
        }
      }
    }
  }
}
