package com.faktocraft.common.scan;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ScanChannel {
  public static final int MAX_CHUNKS = 8192;

  private static final String[] REMOVED_ENTRIES = { "faktocraft:sulfur_ore", "faktocraft:deepslate_sulfur_ore" };

  private final CompoundTag scans;
  private final Runnable onChange;
  private int revision;

  ScanChannel(CompoundTag scans, Runnable onChange) {
    this.scans = scans;
    this.onChange = onChange;
  }

  public static String key(Level level, int chunkX, int chunkZ) {
    return level.dimension().identifier() + "|" + chunkX + "|" + chunkZ;
  }

  public static String localKey(int chunkX, int chunkZ) {
    return chunkX + "," + chunkZ;
  }

  public static CompoundTag sanitize(CompoundTag scan) {
    if (scan.contains("entries")) {
      CompoundTag entries = scan.getCompoundOrEmpty("entries");
      for (String removed : REMOVED_ENTRIES) {
        entries.remove(removed);
      }
    }
    return scan;
  }

  static CompoundTag sanitizeAll(CompoundTag scans) {
    for (String key : scans.keySet()) {
      sanitize(scans.getCompoundOrEmpty(key));
    }
    return scans;
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
    return scans.contains(key) ? scans.getCompoundOrEmpty(key) : null;
  }

  public void put(Level level, int chunkX, int chunkZ, CompoundTag scan) {
    scans.put(key(level, chunkX, chunkZ), sanitize(scan));
    evictOldest();
    bump();
  }

  public void importKeyed(CompoundTag legacy) {
    for (String key : legacy.keySet()) {
      scans.put(key, sanitize(legacy.getCompoundOrEmpty(key).copy()));
    }
    evictOldest();
    bump();
  }

  public void importLocal(Level level, CompoundTag legacy) {
    for (String key : legacy.keySet()) {
      String[] parts = key.split(",");
      if (parts.length != 2) {
        continue;
      }
      try {
        scans.put(key(level, Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
            sanitize(legacy.getCompoundOrEmpty(key).copy()));
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
          out.put(localKey(cx, cz), scans.getCompoundOrEmpty(key).copy());
        }
      }
    }
    return out;
  }

  public CompoundTag collectDimension(Level level) {
    String prefix = level.dimension().identifier() + "|";
    CompoundTag out = new CompoundTag();
    for (String key : scans.keySet()) {
      if (!key.startsWith(prefix)) {
        continue;
      }
      String[] parts = key.substring(prefix.length()).split("\\|");
      if (parts.length != 2) {
        continue;
      }
      out.put(localKey(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])), scans.getCompoundOrEmpty(key).copy());
    }
    return out;
  }

  CompoundTag raw() {
    return scans;
  }

  private void evictOldest() {
    while (scans.keySet().size() > MAX_CHUNKS) {
      String oldest = null;
      long oldestTime = Long.MAX_VALUE;
      for (String key : scans.keySet()) {
        long t = scans.getCompoundOrEmpty(key).getLongOr("t", 0L);
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
