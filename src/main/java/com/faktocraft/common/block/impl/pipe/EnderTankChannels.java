package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.util.LegacySavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.HashMap;
import java.util.Map;

public class EnderTankChannels extends SavedData {

  public static final int CAPACITY_MB = BlockEntityTank.CAPACITY_MB;
  public static final int CODE_NONE = -1;
  public static final int CODE_DIGITS = 6;
  public static final int MAX_CODE = 999_999;

  private static final SavedDataType<EnderTankChannels> TYPE = LegacySavedData.type("ender_tanks",
      EnderTankChannels::new, EnderTankChannels::load, data -> data.save(new CompoundTag()));

  private final Map<Integer, FluidStorage> channels = new HashMap<>();

  public static EnderTankChannels get(ServerLevel level) {
    ServerLevel host = level.getServer().overworld();
    return LegacySavedData.get(host, TYPE, EnderTankChannels::load);
  }

  public static boolean isValidCode(int code) {
    return code >= 0 && code <= MAX_CODE;
  }

  public FluidStorage channel(int code) {
    return channels.computeIfAbsent(code, key -> track(new FluidStorage(CAPACITY_MB)));
  }

  private FluidStorage track(FluidStorage storage) {
    storage.setChangeListener(this::setDirty);
    return storage;
  }

  private static EnderTankChannels load(CompoundTag tag) {
    EnderTankChannels data = new EnderTankChannels();
    ListTag list = tag.getListOrEmpty("channels");
    for (int i = 0; i < list.size(); i++) {
      CompoundTag entry = list.getCompoundOrEmpty(i);
      FluidStorage storage = data.track(new FluidStorage(CAPACITY_MB));
      storage.load(entry.getCompoundOrEmpty("tank"));
      if (!storage.isEmpty() && isValidCode(entry.getIntOr("code", 0))) {
        data.channels.put(entry.getIntOr("code", 0), storage);
      }
    }
    return data;
  }

  public CompoundTag save(CompoundTag tag) {
    ListTag list = new ListTag();
    channels.forEach((code, storage) -> {
      if (storage.isEmpty()) {
        return;
      }
      CompoundTag entry = new CompoundTag();
      entry.putInt("code", code);
      CompoundTag tankTag = new CompoundTag();
      storage.save(tankTag);
      entry.put("tank", tankTag);
      list.add(entry);
    });
    tag.put("channels", list);
    return tag;
  }
}
