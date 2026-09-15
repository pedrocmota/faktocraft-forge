package com.faktocraft.common.scan;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ScanChannel {

  public static final int MAX_CHUNKS = 8192;

  private final CompoundTag scans;
  private final Runnable onChange;
  private int revision;

  ScanChannel(CompoundTag scans, Runnable onChange) {
    this.scans = scans;
    this.onChange = onChange;
  }

  public static String key(Level level, int chunkX, int chunkZ) {
    return level.dimension().location() + "|" + chunkX + "|" + chunkZ;
  }

  public static String localKey(int chunkX, int chunkZ) {
    return chunkX + "," + chunkZ;
  }

  public int revision() {
    return revision;
  }

  public boolean isEmpty() {
    return scans.isEmpty();
  }

  public boolean has(Level level, int chunkX, int chunkZ) {
    return scans.contains(key(level, chunkX, chunkZ));
  }

  @Nullable
  public CompoundTag get(Level level, int chunkX, int chunkZ) {
    String key = key(level, chunkX, chunkZ);
    return scans.contains(key) ? scans.getCompound(key) : null;
  }

  public void put(Level level, int chunkX, int chunkZ, CompoundTag scan) {
    scans.put(key(level, chunkX, chunkZ), scan);
    evictOldest();
    bump();
  }

  public void importKeyed(CompoundTag legacy) {
    for (String key : legacy.getAllKeys()) {
      scans.put(key, legacy.getCompound(key).copy());
    }
    evictOldest();
    bump();
  }

  public void importLocal(Level level, CompoundTag legacy) {
    for (String key : legacy.getAllKeys()) {
      String[] parts = key.split(",");
      if (parts.length != 2) {
        continue;
      }
      try {
        scans.put(key(level, Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
            legacy.getCompound(key).copy());
      } catch (NumberFormatException ignored) {
        continue;
      }
    }
    evictOldest();
    bump();
  }

  public CompoundTag collect(Level level, int centerX, int centerZ, int radius) {
    CompoundTag out = new CompoundTag();
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dz = -radius; dz <= radius; dz++) {
        int cx = centerX + dx;
        int cz = centerZ + dz;
        String key = key(level, cx, cz);
        if (scans.contains(key)) {
          out.put(localKey(cx, cz), scans.getCompound(key).copy());
        }
      }
    }
    return out;
  }

  public CompoundTag collectDimension(Level level) {
    String prefix = level.dimension().location() + "|";
    CompoundTag out = new CompoundTag();
    for (String key : scans.getAllKeys()) {
      if (!key.startsWith(prefix)) {
        continue;
      }
      String[] parts = key.substring(prefix.length()).split("\\|");
      if (parts.length != 2) {
        continue;
      }
      out.put(localKey(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])), scans.getCompound(key).copy());
    }
    return out;
  }

  CompoundTag raw() {
    return scans;
  }

  private void evictOldest() {
    while (scans.getAllKeys().size() > MAX_CHUNKS) {
      String oldest = null;
      long oldestTime = Long.MAX_VALUE;
      for (String key : scans.getAllKeys()) {
        long t = scans.getCompound(key).getLong("t");
        if (t < oldestTime) {
          oldestTime = t;
          oldest = key;
        }
      }
      if (oldest == null) {
        return;
      }
      scans.remove(oldest);
    }
  }

  private void bump() {
    revision++;
    onChange.run();
  }
}
