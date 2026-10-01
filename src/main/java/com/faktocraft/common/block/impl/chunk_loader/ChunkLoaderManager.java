package com.faktocraft.common.block.impl.chunk_loader;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.util.LegacySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChunkLoaderManager extends SavedData {
  private static final SavedDataType<ChunkLoaderManager> TYPE = LegacySavedData.type("chunk_loaders",
      ChunkLoaderManager::new, ChunkLoaderManager::load, manager -> manager.save(new CompoundTag()));

  public static final TicketController TICKETS = new TicketController(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "chunk_loaders"), ChunkLoaderManager::validateTickets);

  private final Set<GlobalPos> active = new LinkedHashSet<>();

  public static void register(RegisterTicketControllersEvent event) {
    event.register(TICKETS);
  }

  public static ChunkLoaderManager get(MinecraftServer server) {
    return storageFor(server.overworld());
  }

  public static ChunkLoaderManager get(ServerLevel level) {
    ServerLevel overworld = level.getServer().overworld();

    return storageFor(overworld != null ? overworld : level);
  }

  private static ChunkLoaderManager storageFor(ServerLevel host) {
    return LegacySavedData.get(host, TYPE, ChunkLoaderManager::load);
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

  public static void validateTickets(ServerLevel level, TicketHelper helper) {
    ChunkLoaderManager manager = get(level);
    for (BlockPos owner : List.copyOf(helper.getBlockTickets().keySet())) {
      if (!manager.isActive(GlobalPos.of(level.dimension(), owner))) {
        helper.removeAllTickets(owner);
      }
    }
  }

  public static void reloadTickets(MinecraftServer server) {
    ChunkLoaderManager manager = get(server);
    for (GlobalPos key : List.copyOf(manager.active)) {
      ServerLevel level = server.getLevel(key.dimension());
      if (level == null) {
        continue;
      }
      BlockPos owner = key.pos();
      ChunkPos chunk = ChunkPos.containing(owner);
      TICKETS.forceChunk(level, owner, chunk.x(), chunk.z(), true, true);
      if (!(level.getBlockEntity(owner) instanceof BlockEntityChunkLoader)) {
        TICKETS.forceChunk(level, owner, chunk.x(), chunk.z(), false, true);
      }
    }
  }

  private static ChunkLoaderManager load(CompoundTag tag) {
    ChunkLoaderManager manager = new ChunkLoaderManager();
    ListTag list = tag.getListOrEmpty("loaders");
    for (int i = 0; i < list.size(); i++) {
      CompoundTag entry = list.getCompoundOrEmpty(i);
      Identifier dimension = Identifier.tryParse(entry.getStringOr("dim", ""));
      if (dimension == null) {
        continue;
      }
      manager.active.add(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension),
          BlockPos.of(entry.getLongOr("pos", 0L))));
    }
    return manager;
  }

  public CompoundTag save(CompoundTag tag) {
    ListTag list = new ListTag();
    for (GlobalPos pos : active) {
      CompoundTag entry = new CompoundTag();
      entry.putString("dim", pos.dimension().identifier().toString());
      entry.putLong("pos", pos.pos().asLong());
      list.add(entry);
    }
    tag.put("loaders", list);
    return tag;
  }
}
