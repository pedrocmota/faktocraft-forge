package com.faktocraft.gametest.tools;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class ToolEnchantGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static boolean accepts(Item item, Enchantment enchantment) {
    return enchantment.canEnchant(new ItemStack(item));
  }

  private static void expect(GameTestHelper helper, Item item, Enchantment enchantment, boolean expected) {
    if (accepts(item, enchantment) != expected) {
      helper.fail(item + (expected ? " refuses " : " accepts ") + enchantment.getDescriptionId());
    }
  }

  @GameTest(template = TEMPLATE)
  public static void drillsTakeMiningEnchantments(GameTestHelper helper) {
    for (Item item : new Item[] { ModItems.MINING_DRILL, ModItems.DIAMOND_DRILL, ModItems.IRIDIUM_DRILL,
        ModItems.MULTI_TOOL, ModItems.ELECTRIC_HOE }) {
      expect(helper, item, Enchantments.BLOCK_EFFICIENCY, true);
      expect(helper, item, Enchantments.BLOCK_FORTUNE, true);
      expect(helper, item, Enchantments.SILK_TOUCH, true);
      expect(helper, item, Enchantments.SHARPNESS, false);
      expect(helper, item, Enchantments.UNBREAKING, false);
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void chainsawsTakeMiningAndSharpEnchantments(GameTestHelper helper) {
    for (Item item : new Item[] { ModItems.CHAINSAW, ModItems.DIAMOND_CHAINSAW, ModItems.IRIDIUM_CHAINSAW }) {
      expect(helper, item, Enchantments.BLOCK_EFFICIENCY, true);
      expect(helper, item, Enchantments.BLOCK_FORTUNE, true);
      expect(helper, item, Enchantments.SILK_TOUCH, true);
      expect(helper, item, Enchantments.SHARPNESS, true);
      expect(helper, item, Enchantments.SMITE, true);
      expect(helper, item, Enchantments.BANE_OF_ARTHROPODS, true);
      expect(helper, item, Enchantments.FIRE_ASPECT, false);
      expect(helper, item, Enchantments.MOB_LOOTING, false);
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void nanoSaberTakesMeleeEnchantments(GameTestHelper helper) {
    expect(helper, ModItems.NANO_SABER, Enchantments.SHARPNESS, true);
    expect(helper, ModItems.NANO_SABER, Enchantments.FIRE_ASPECT, true);
    expect(helper, ModItems.NANO_SABER, Enchantments.MOB_LOOTING, true);
    expect(helper, ModItems.NANO_SABER, Enchantments.KNOCKBACK, true);
    expect(helper, ModItems.NANO_SABER, Enchantments.BLOCK_EFFICIENCY, false);
    expect(helper, ModItems.NANO_SABER, Enchantments.UNBREAKING, false);
    helper.succeed();
  }
}
