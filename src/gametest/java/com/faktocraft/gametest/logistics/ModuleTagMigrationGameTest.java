package com.faktocraft.gametest.logistics;

import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import com.faktocraft.common.util.LegacyTags;
import com.faktocraft.gametest.GameTest;
import java.util.Map;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModuleTagMigrationGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final Map<String, String> RENAMES = Map.ofEntries(
      Map.entry("forge:ingots/iron", "c:ingots/iron"),
      Map.entry("forge:dusts/sulfur", "c:dusts/sulfur"),
      Map.entry("forge:glass", "c:glass_blocks"),
      Map.entry("forge:glass/colorless", "c:glass_blocks/colorless"),
      Map.entry("forge:cobblestone", "c:cobblestones"),
      Map.entry("forge:cobblestone/deepslate", "c:cobblestones/deepslate"),
      Map.entry("forge:sand", "c:sands"),
      Map.entry("forge:sand/red", "c:sands/red"),
      Map.entry("forge:string", "c:strings"),
      Map.entry("forge:slimeballs", "c:slime_balls"),
      Map.entry("forge:heads", "c:skulls"),
      Map.entry("forge:shears", "c:tools/shear"),
      Map.entry("forge:tools/swords", "minecraft:swords"),
      Map.entry("minecraft:logs", "minecraft:logs"),
      Map.entry("c:ingots/iron", "c:ingots/iron"));

  @GameTest(template = TEMPLATE)
  public static void legacyTagNamesMigrate(GameTestHelper helper) {
    for (Map.Entry<String, String> entry : RENAMES.entrySet()) {
      String migrated = LegacyTags.migrate(entry.getKey());
      if (!migrated.equals(entry.getValue())) {
        helper.fail(entry.getKey() + " migrated to " + migrated + ", expected " + entry.getValue());
        return;
      }
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void moduleTagFiltersMigrateOnLoad(GameTestHelper helper) {
    ItemStack module = new ItemStack(Items.PAPER);
    ModuleSettings.setLine(module, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.TAG, ItemStack.EMPTY,
        "forge:ingots/iron", false, false, 0));
    ModuleSettings.setLine(module, 1, new ModuleSettings.FilterLine(ModuleSettings.LineMode.NAMESPACE,
        ItemStack.EMPTY, "forge:keep", false, false, 0));
    ModuleSettings.FilterLine tagLine = ModuleSettings.lines(module).get(0);
    ModuleSettings.FilterLine namespaceLine = ModuleSettings.lines(module).get(1);
    if (!tagLine.text().equals("c:ingots/iron")) {
      helper.fail("tag filter was not migrated: " + tagLine.text());
      return;
    }
    if (!namespaceLine.text().equals("forge:keep")) {
      helper.fail("non-tag line text was rewritten: " + namespaceLine.text());
      return;
    }
    if (!ModuleSettings.anyLineMatches(module, new ItemStack(Items.IRON_INGOT))) {
      helper.fail("migrated tag filter does not match iron ingots");
      return;
    }
    helper.succeed();
  }
}
