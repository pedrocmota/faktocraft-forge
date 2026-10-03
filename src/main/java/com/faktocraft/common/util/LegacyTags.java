package com.faktocraft.common.util;

import java.util.Map;

public final class LegacyTags {
  private static final Map<String, String> RENAMED = Map.ofEntries(
      Map.entry("glass", "c:glass_blocks"),
      Map.entry("glass/colorless", "c:glass_blocks/colorless"),
      Map.entry("glass/silica", "c:glass_blocks/cheap"),
      Map.entry("glass/tinted", "c:glass_blocks/tinted"),
      Map.entry("stained_glass", "c:glass_blocks"),
      Map.entry("stained_glass_panes", "c:glass_panes"),
      Map.entry("sandstone", "c:sandstone/blocks"),
      Map.entry("shears", "c:tools/shear"),
      Map.entry("tools/shields", "c:tools/shield"),
      Map.entry("tools/bows", "c:tools/bow"),
      Map.entry("tools/crossbows", "c:tools/crossbow"),
      Map.entry("tools/fishing_rods", "c:tools/fishing_rod"),
      Map.entry("tools/tridents", "c:tools/spear"),
      Map.entry("tools/swords", "minecraft:swords"),
      Map.entry("tools/axes", "minecraft:axes"),
      Map.entry("tools/pickaxes", "minecraft:pickaxes"),
      Map.entry("tools/shovels", "minecraft:shovels"),
      Map.entry("tools/hoes", "minecraft:hoes"),
      Map.entry("playerworkstations", "c:player_workstations"));

  private static final Map<String, String> RENAMED_ROOTS = Map.ofEntries(
      Map.entry("gravel", "gravels"),
      Map.entry("stone", "stones"),
      Map.entry("cobblestone", "cobblestones"),
      Map.entry("obsidian", "obsidians"),
      Map.entry("netherrack", "netherracks"),
      Map.entry("sand", "sands"),
      Map.entry("string", "strings"),
      Map.entry("leather", "leathers"),
      Map.entry("slimeballs", "slime_balls"),
      Map.entry("heads", "skulls"),
      Map.entry("gunpowder", "gunpowders"));

  private LegacyTags() {
  }

  public static String migrate(String tag) {
    if (tag == null || !tag.startsWith("forge:")) {
      return tag;
    }
    String path = tag.substring("forge:".length());
    String exact = RENAMED.get(path);
    if (exact != null) {
      return exact;
    }
    int slash = path.indexOf('/');
    String root = slash < 0 ? path : path.substring(0, slash);
    String renamedRoot = RENAMED_ROOTS.get(root);
    if (renamedRoot != null) {
      return "c:" + renamedRoot + (slash < 0 ? "" : path.substring(slash));
    }
    return "c:" + path;
  }
}
