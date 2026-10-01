package com.faktocraft.common.util;

import java.util.Map;

public final class LegacyTags {
  private static final Map<String, String> RENAMED_ROOTS = Map.of(
      "gravel", "gravels",
      "stone", "stones",
      "cobblestone", "cobblestones",
      "obsidian", "obsidians",
      "glass/colorless", "glass_blocks/colorless");

  private LegacyTags() {
  }

  public static String migrate(String tag) {
    if (tag == null || !tag.startsWith("forge:")) {
      return tag;
    }
    String path = tag.substring("forge:".length());
    return "c:" + RENAMED_ROOTS.getOrDefault(path, path);
  }
}
