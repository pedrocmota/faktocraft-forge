package com.faktocraft.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.faktocraft.IndReb;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static ServerConfig server = new ServerConfig();

  public static ServerConfig server() {
    return server;
  }

  public static void register() {
    Path path = FMLPaths.CONFIGDIR.get().resolve("faktocraft.json");
    try {
      if (Files.exists(path)) {
        try (var reader = Files.newBufferedReader(path)) {
          ServerConfig loaded = GSON.fromJson(reader, ServerConfig.class);
          if (loaded != null) {
            server = loaded;
          }
        }
      }
      Files.writeString(path, GSON.toJson(server));
    } catch (IOException | RuntimeException e) {
      IndReb.LOGGER.error("Failed to load config, using defaults", e);
    }
  }
}
