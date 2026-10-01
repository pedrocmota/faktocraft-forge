package com.faktocraft.gametest.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LegacyFixture {
  public static final String PROPERTY = "faktocraft.legacyFixture";
  public static final String MODE_BUILD = "build";
  public static final String MODE_VERIFY = "verify";

  public static final int BASE_X = 2000;
  public static final int BASE_Z = 2000;
  public static final int LOADER_DX = 40;
  public static final int LOADER_DZ = 40;

  public static final Path DIR = Path.of("legacy-fixture");
  public static final Path MANIFEST = DIR.resolve("manifest.json");
  public static final Path NBT_DIR = DIR.resolve("nbt");

  public static final String SULFUR_ORE_ID = "faktocraft:sulfur_ore";
  public static final String DEEPSLATE_SULFUR_ORE_ID = "faktocraft:deepslate_sulfur_ore";

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  private LegacyFixture() {
  }

  private static final String[][] SEEDED_DIRS = {
      { "region", "dimensions/minecraft/overworld/region" },
      { "entities", "dimensions/minecraft/overworld/entities" },
      { "poi", "dimensions/minecraft/overworld/poi" },
      { "DIM-1/region", "dimensions/minecraft/the_nether/region" },
      { "DIM-1/entities", "dimensions/minecraft/the_nether/entities" },
      { "DIM1/region", "dimensions/minecraft/the_end/region" },
      { "DIM1/entities", "dimensions/minecraft/the_end/entities" },
      { "data", "data" },
      { "DIM-1/data", "DIM-1/data" },
      { "DIM1/data", "DIM1/data" } };

  public static void seedWorld(net.minecraft.server.MinecraftServer server) {
    if (!MODE_VERIFY.equals(mode())) {
      return;
    }
    Path source = Path.of("world");
    if (!Files.isDirectory(source.resolve("region"))) {
      com.faktocraft.Faktocraft.LOGGER.warn("[LegacyFixture] verify mode but no fixture world at {}",
          source.toAbsolutePath());
      return;
    }
    Path target = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATA).getParent();
    try {
      for (String[] mapping : SEEDED_DIRS) {
        Path from = source.resolve(mapping[0]);
        if (!Files.isDirectory(from)) {
          continue;
        }
        Path toRoot = target.resolve(mapping[1]);
        try (java.util.stream.Stream<Path> paths = Files.walk(from)) {
          for (Path path : (Iterable<Path>) paths::iterator) {
            Path to = toRoot.resolve(from.relativize(path).toString());
            if (Files.isDirectory(path)) {
              Files.createDirectories(to);
            } else {
              Files.createDirectories(to.getParent());
              Files.copy(path, to, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
          }
        }
      }
      com.faktocraft.Faktocraft.LOGGER.info("[LegacyFixture] seeded {} from {}", target, source.toAbsolutePath());
    } catch (IOException e) {
      throw new UncheckedIOException("Could not seed the legacy fixture world", e);
    }
  }

  public static String mode() {
    return System.getProperty(PROPERTY, "").trim().toLowerCase(java.util.Locale.ROOT);
  }

  public static boolean afterPort() {
    return !BuiltInRegistries.BLOCK.containsKey(Identifier.parse(SULFUR_ORE_ID));
  }

  public static JsonArray pos(BlockPos pos) {
    JsonArray array = new JsonArray();
    array.add(pos.getX());
    array.add(pos.getY());
    array.add(pos.getZ());
    return array;
  }

  public static BlockPos pos(JsonObject entry) {
    JsonArray array = entry.getAsJsonArray("pos");
    return new BlockPos(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
  }

  public static BlockPos pos(JsonObject entry, String key) {
    JsonArray array = entry.getAsJsonArray(key);
    return new BlockPos(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
  }

  public static void writeManifest(JsonObject manifest) {
    try {
      Files.createDirectories(DIR);
      Files.writeString(MANIFEST, GSON.toJson(manifest), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static JsonObject readManifest() {
    try {
      return JsonParser.parseString(Files.readString(MANIFEST, StandardCharsets.UTF_8)).getAsJsonObject();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void writeNbt(String name, CompoundTag tag) {
    try {
      Files.createDirectories(NBT_DIR);
      NbtIo.writeCompressed(tag, NBT_DIR.resolve(name + ".nbt"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static CompoundTag readNbt(String name) {
    try {
      return NbtIo.readCompressed(NBT_DIR.resolve(name + ".nbt"), NbtAccounter.unlimitedHeap());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static boolean hasNbt(String name) {
    return Files.exists(NBT_DIR.resolve(name + ".nbt"));
  }
}
