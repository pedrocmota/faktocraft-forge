package com.faktocraft.common.scan;

import com.faktocraft.common.util.LegacySavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScanChannels extends SavedData {

  public static final int DEFAULT_CODE = 0;
  public static final int CODE_DIGITS = 6;
  public static final int MAX_CODE = 999_999;

  private static final SavedDataType<ScanChannels> TYPE = LegacySavedData.type("scan_channels",
      ScanChannels::new, ScanChannels::load, data -> data.save(new CompoundTag()));

  private final Map<Integer, ScanChannel> channels = new HashMap<>();

  public static ScanChannels get(ServerLevel level) {
    ServerLevel host = level.getServer().overworld();
    return LegacySavedData.get(host, TYPE, ScanChannels::load);
  }

  public static boolean isValidCode(int code) {
    return code >= 0 && code <= MAX_CODE;
  }

  public static int sanitize(int code) {
    return isValidCode(code) ? code : DEFAULT_CODE;
  }

  public static String codeText(int code) {
    return String.format(Locale.ROOT, "%0" + CODE_DIGITS + "d", sanitize(code));
  }

  public ScanChannel channel(int code) {
    return channels.computeIfAbsent(sanitize(code), key -> new ScanChannel(new CompoundTag(), this::setDirty));
  }

  private static ScanChannels load(CompoundTag tag) {
    ScanChannels data = new ScanChannels();
    ListTag list = tag.getListOrEmpty("channels");
    for (int i = 0; i < list.size(); i++) {
      CompoundTag entry = list.getCompoundOrEmpty(i);
      int code = entry.getIntOr("code", 0);
      if (isValidCode(code)) {
        data.channels.put(code, new ScanChannel(ScanChannel.sanitizeAll(entry.getCompoundOrEmpty("scans").copy()),
            data::setDirty));
      }
    }
    return data;
  }

  public CompoundTag save(CompoundTag tag) {
    ListTag list = new ListTag();
    channels.forEach((code, channel) -> {
      if (channel.isEmpty()) {
        return;
      }
      CompoundTag entry = new CompoundTag();
      entry.putInt("code", code);
      entry.put("scans", channel.raw().copy());
      list.add(entry);
    });
    tag.put("channels", list);
    return tag;
  }
}
