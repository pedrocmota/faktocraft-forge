package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.entity.block.FluidStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;

public class EnderTankChannels extends SavedData {

  public static final int CAPACITY_MB = BlockEntityTank.CAPACITY_MB;
  public static final int CODE_NONE = -1;
  public static final int CODE_DIGITS = 6;
  public static final int MAX_CODE = 999_999;

  private static final String DATA_NAME = "faktocraft_ender_tanks";

  private final Map<Integer, FluidStorage> channels = new HashMap<>();

  public static EnderTankChannels get(ServerLevel level) {
    ServerLevel host = level.getServer().overworld();
    return host.getDataStorage().computeIfAbsent(EnderTankChannels::load, EnderTankChannels::new, DATA_NAME);
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
    ListTag list = tag.getList("channels", Tag.TAG_COMPOUND);
    for (int i = 0; i < list.size(); i++) {
      CompoundTag entry = list.getCompound(i);
      FluidStorage storage = data.track(new FluidStorage(CAPACITY_MB));
      storage.load(entry.getCompound("tank"));
      if (!storage.isEmpty() && isValidCode(entry.getInt("code"))) {
        data.channels.put(entry.getInt("code"), storage);
      }
    }
    return data;
  }

  @Override
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
