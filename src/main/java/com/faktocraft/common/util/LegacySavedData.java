package com.faktocraft.common.util;

import com.faktocraft.Faktocraft;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LegacySavedData {
  private LegacySavedData() {
  }

  public static <T extends SavedData> SavedDataType<T> type(String name, Supplier<T> constructor,
      Function<CompoundTag, T> loader, Function<T, CompoundTag> saver) {
    Codec<T> codec = NbtBridge.WHOLE.codec().xmap(loader, saver);
    return new SavedDataType<>(Identifier.fromNamespaceAndPath(Faktocraft.MODID, name), constructor, codec, null);
  }

  public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type,
      Function<CompoundTag, T> loader) {
    T existing = level.getDataStorage().get(type);
    if (existing != null) {
      return existing;
    }
    T imported = importLegacy(level, type, loader);
    if (imported != null) {
      level.getDataStorage().set(type, imported);
      imported.setDirty();
      return imported;
    }
    return level.getDataStorage().computeIfAbsent(type);
  }

  public static Path legacyDataFolder(ServerLevel level) {
    Path root = level.getServer().getWorldPath(LevelResource.ROOT);
    if (level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
      return root.resolve("data");
    }
    if (level.dimension() == net.minecraft.world.level.Level.NETHER) {
      return root.resolve("DIM-1").resolve("data");
    }
    if (level.dimension() == net.minecraft.world.level.Level.END) {
      return root.resolve("DIM1").resolve("data");
    }
    return DimensionType.getStorageFolder(level.dimension(), root).resolve("data");
  }

  private static <T extends SavedData> T importLegacy(ServerLevel level, SavedDataType<T> type,
      Function<CompoundTag, T> loader) {
    String fileName = Faktocraft.MODID + "_" + type.id().getPath() + ".dat";
    Path legacy = legacyDataFolder(level).resolve(fileName);
    if (!Files.isRegularFile(legacy)) {
      legacy = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT))
          .resolve("data").resolve(fileName);
      if (!Files.isRegularFile(legacy)) {
        return null;
      }
    }
    try {
      CompoundTag file = NbtIo.readCompressed(legacy, NbtAccounter.unlimitedHeap());
      CompoundTag data = file.getCompoundOrEmpty("data");
      T value = NbtBridge.withResult(level.registryAccess(), () -> loader.apply(data));
      Faktocraft.LOGGER.info("Imported legacy saved data {} from {}", type.id(), legacy.getFileName());
      return value;
    } catch (Exception e) {
      Faktocraft.LOGGER.error("Could not import legacy saved data {} from {}", type.id(), legacy, e);
      return null;
    }
  }
}
