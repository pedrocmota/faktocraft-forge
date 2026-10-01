package com.faktocraft.gametest.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
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

  public static String mode() {
    return System.getProperty(PROPERTY, "").trim().toLowerCase(java.util.Locale.ROOT);
  }

  public static boolean afterPort() {
    return !ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(SULFUR_ORE_ID));
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
      NbtIo.writeCompressed(tag, NBT_DIR.resolve(name + ".nbt").toFile());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static CompoundTag readNbt(String name) {
    try {
      return NbtIo.readCompressed(NBT_DIR.resolve(name + ".nbt").toFile());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static boolean hasNbt(String name) {
    return Files.exists(NBT_DIR.resolve(name + ".nbt"));
  }
}
