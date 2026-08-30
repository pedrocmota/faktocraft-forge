package com.faktocraft.common.block.impl.chunk_loader;

import com.faktocraft.common.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.world.ForgeChunkManager;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChunkLoaderManager extends SavedData {

  private static final String DATA_NAME = "faktocraft_chunk_loaders";

  private final Set<GlobalPos> active = new LinkedHashSet<>();

  public static ChunkLoaderManager get(MinecraftServer server) {
    return storageFor(server.overworld());
  }

  public static ChunkLoaderManager get(ServerLevel level) {
    ServerLevel overworld = level.getServer().overworld();

    return storageFor(overworld != null ? overworld : level);
  }

  private static ChunkLoaderManager storageFor(ServerLevel host) {
    return host.getDataStorage().computeIfAbsent(ChunkLoaderManager::load, ChunkLoaderManager::new, DATA_NAME);
  }

  public static int maxActive() {
    return Math.max(0, ModConfig.server().chunk_loader_max_active);
  }

  public boolean tryActivate(GlobalPos pos) {
    if (active.contains(pos)) {
      return true;
    }
    if (active.size() >= maxActive()) {
      return false;
    }
    active.add(pos);
    setDirty();
    return true;
  }

  public void deactivate(GlobalPos pos) {
    if (active.remove(pos)) {
      setDirty();
    }
  }

  public boolean isActive(GlobalPos pos) {
    return active.contains(pos);
  }

  public int activeCount() {
    return active.size();
  }

  public static void validateTickets(ServerLevel level, ForgeChunkManager.TicketHelper helper) {
    ChunkLoaderManager manager = get(level);
    for (BlockPos owner : List.copyOf(helper.getBlockTickets().keySet())) {
      if (!manager.isActive(GlobalPos.of(level.dimension(), owner))) {
        helper.removeAllTickets(owner);
      }
    }
  }

  private static ChunkLoaderManager load(CompoundTag tag) {
    ChunkLoaderManager manager = new ChunkLoaderManager();
    ListTag list = tag.getList("loaders", Tag.TAG_COMPOUND);
    for (int i = 0; i < list.size(); i++) {
      CompoundTag entry = list.getCompound(i);
      ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("dim"));
      if (dimension == null) {
        continue;
      }
      manager.active.add(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension),
          BlockPos.of(entry.getLong("pos"))));
    }
    return manager;
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    ListTag list = new ListTag();
    for (GlobalPos pos : active) {
      CompoundTag entry = new CompoundTag();
      entry.putString("dim", pos.dimension().location().toString());
      entry.putLong("pos", pos.pos().asLong());
      list.add(entry);
    }
    tag.put("loaders", list);
    return tag;
  }
}
