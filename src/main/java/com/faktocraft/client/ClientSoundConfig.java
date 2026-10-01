package com.faktocraft.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientSoundConfig {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static Float machineVolume = null;

  private ClientSoundConfig() {
  }

  private static Path file() {
    return FMLPaths.CONFIGDIR.get().resolve("faktocraft-client.json");
  }

  public static float machineVolume() {
    if (machineVolume == null) {
      machineVolume = 1.0F;
      try {
        Path path = file();
        if (Files.exists(path)) {
          JsonObject json = GSON.fromJson(Files.readString(path), JsonObject.class);
          if (json != null && json.has("machine_sound_volume")) {
            machineVolume = Math.max(0.0F, Math.min(1.0F, json.get("machine_sound_volume").getAsFloat()));
          }
        }
      } catch (Exception ignored) {
      }
    }
    return machineVolume;
  }

  public static void setMachineVolume(float volume) {
    machineVolume = Math.max(0.0F, Math.min(1.0F, volume));
    try {
      JsonObject json = new JsonObject();
      Path path = file();
      if (Files.exists(path)) {
        JsonObject existing = GSON.fromJson(Files.readString(path), JsonObject.class);
        if (existing != null) {
          json = existing;
        }
      }
      json.addProperty("machine_sound_volume", machineVolume);
      Files.writeString(path, GSON.toJson(json));
    } catch (IOException ignored) {
    }
  }
}
