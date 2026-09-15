package com.faktocraft.common.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class RadiationSources extends SavedData {

  private static final String DATA_NAME = "faktocraft_radiation_sources";

  private final Map<String, Set<Long>> byDimension = new HashMap<>();
  private final Map<String, List<Aftermath>> aftermathByDimension = new HashMap<>();

  public record Aftermath(long pos, float strength, long started, long expires) {
    public float remaining(long time) {
      long span = Math.max(1L, expires - started);
      return Math.max(0.0F, Math.min(1.0F, (expires - time) / (float) span));
    }
  }

  public static RadiationSources get(ServerLevel level) {
    ServerLevel host = level.getServer().overworld();
    return host.getDataStorage().computeIfAbsent(RadiationSources::load, RadiationSources::new, DATA_NAME);
  }

  private static String key(ServerLevel level) {
    return level.dimension().location().toString();
  }

  public void add(ServerLevel level, BlockPos pos) {
    if (byDimension.computeIfAbsent(key(level), k -> new HashSet<>()).add(pos.asLong())) {
      setDirty();
    }
  }

  public void remove(ServerLevel level, BlockPos pos) {
    Set<Long> set = byDimension.get(key(level));
    if (set != null && set.remove(pos.asLong())) {
      setDirty();
    }
  }

  public Set<Long> positions(ServerLevel level) {
    return byDimension.getOrDefault(key(level), Set.of());
  }

  public void addAftermath(ServerLevel level, BlockPos pos, float strength, long durationTicks) {
    long now = level.getGameTime();
    aftermathByDimension.computeIfAbsent(key(level), k -> new ArrayList<>())
        .add(new Aftermath(pos.asLong(), strength, now, now + Math.max(1L, durationTicks)));
    setDirty();
  }

  public int clearAftermathNear(ServerLevel level, BlockPos center, double radius) {
    List<Aftermath> list = aftermathByDimension.get(key(level));
    if (list == null) {
      return 0;
    }
    double radiusSq = radius * radius;
    int before = list.size();
    list.removeIf(entry -> BlockPos.of(entry.pos()).distSqr(center) <= radiusSq);
    int removed = before - list.size();
    if (removed > 0) {
      setDirty();
    }
    return removed;
  }

  public List<Aftermath> aftermath(ServerLevel level) {
    List<Aftermath> list = aftermathByDimension.get(key(level));
    if (list == null) {
      return List.of();
    }
    long now = level.getGameTime();
    if (list.removeIf(entry -> entry.expires() <= now)) {
      setDirty();
    }
    return list;
  }

  public void prune(ServerLevel level, Predicate<BlockPos> stillValid) {
    Set<Long> set = byDimension.get(key(level));
    if (set == null) {
      return;
    }
    if (set.removeIf(packed -> !stillValid.test(BlockPos.of(packed)))) {
      setDirty();
    }
  }

  private static RadiationSources load(CompoundTag tag) {
    RadiationSources data = new RadiationSources();
    CompoundTag dims = tag.getCompound("dimensions");
    for (String dim : dims.getAllKeys()) {
      Set<Long> set = new HashSet<>();
      for (Tag entry : dims.getList(dim, Tag.TAG_LONG)) {
        set.add(((LongTag) entry).getAsLong());
      }
      data.byDimension.put(dim, set);
    }
    CompoundTag fallout = tag.getCompound("aftermath");
    for (String dim : fallout.getAllKeys()) {
      List<Aftermath> list = new ArrayList<>();
      for (Tag entry : fallout.getList(dim, Tag.TAG_COMPOUND)) {
        CompoundTag item = (CompoundTag) entry;
        list.add(new Aftermath(item.getLong("pos"), item.getFloat("strength"), item.getLong("started"),
            item.getLong("expires")));
      }
      data.aftermathByDimension.put(dim, list);
    }
    return data;
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    CompoundTag dims = new CompoundTag();
    for (Map.Entry<String, Set<Long>> entry : byDimension.entrySet()) {
      ListTag list = new ListTag();
      for (long packed : entry.getValue()) {
        list.add(LongTag.valueOf(packed));
      }
      dims.put(entry.getKey(), list);
    }
    tag.put("dimensions", dims);
    CompoundTag fallout = new CompoundTag();
    for (Map.Entry<String, List<Aftermath>> entry : aftermathByDimension.entrySet()) {
      ListTag list = new ListTag();
      for (Aftermath item : entry.getValue()) {
        CompoundTag itemTag = new CompoundTag();
        itemTag.putLong("pos", item.pos());
        itemTag.putFloat("strength", item.strength());
        itemTag.putLong("started", item.started());
        itemTag.putLong("expires", item.expires());
        list.add(itemTag);
      }
      fallout.put(entry.getKey(), list);
    }
    tag.put("aftermath", fallout);
    return tag;
  }
}
