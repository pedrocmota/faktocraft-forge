package com.faktocraft.common.block.impl.nuke;

import com.faktocraft.common.util.LegacySavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NukeBlasts extends SavedData {

  private static final SavedDataType<NukeBlasts> TYPE = LegacySavedData.type("nuke_blasts",
      NukeBlasts::new, NukeBlasts::load, data -> data.save(new CompoundTag()));

  private final List<NukeBlast> active = new ArrayList<>();

  public static NukeBlasts get(ServerLevel level) {
    return LegacySavedData.get(level, TYPE, NukeBlasts::load);
  }

  public boolean isEmpty() {
    return active.isEmpty();
  }

  public List<NukeBlast> active() {
    return Collections.unmodifiableList(active);
  }

  public void add(NukeBlast blast) {
    active.add(blast);
    setDirty();
  }

  public void tick(ServerLevel level, int budget) {
    if (active.isEmpty()) {
      return;
    }
    Iterator<NukeBlast> it = active.iterator();
    while (it.hasNext()) {
      NukeBlast blast = it.next();
      blast.step(level, budget);
      if (blast.done()) {
        it.remove();
      }
    }
    setDirty();
  }

  public static NukeBlasts load(CompoundTag tag) {
    NukeBlasts data = new NukeBlasts();
    for (Tag entry : tag.getListOrEmpty("blasts")) {
      NukeBlast blast = NukeBlast.load((CompoundTag) entry);
      if (!blast.done()) {
        data.active.add(blast);
      }
    }
    return data;
  }

  public CompoundTag save(CompoundTag tag) {
    ListTag list = new ListTag();
    for (NukeBlast blast : active) {
      list.add(blast.save(new CompoundTag()));
    }
    tag.put("blasts", list);
    return tag;
  }
}
