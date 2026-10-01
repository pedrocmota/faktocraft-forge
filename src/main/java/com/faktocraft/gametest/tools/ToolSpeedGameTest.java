package com.faktocraft.gametest.tools;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.DiggerElectricItem;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class ToolSpeedGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockState STONE = Blocks.STONE.defaultBlockState();
  private static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();

  private static ItemStack tool(Item item, int efficiency) {
    ItemStack stack = item instanceof ElectricItem electric ? electric.makeFullStack() : new ItemStack(item);
    if (efficiency > 0) {
      stack.enchant(Enchantments.BLOCK_EFFICIENCY, efficiency);
    }
    return stack;
  }

  private static float playerSpeed(ItemStack stack, BlockState block) {
    float speed = stack.getDestroySpeed(block);
    return speed > 1.0F ? speed + DiggerElectricItem.efficiencyBonus(stack) : speed;
  }

  private static void assertRatio(GameTestHelper helper, Item electric, Item vanilla, BlockState block,
      float ratio) {
    for (int level = 0; level <= 5; level++) {
      float electricSpeed = playerSpeed(tool(electric, level), block);
      float vanillaSpeed = playerSpeed(tool(vanilla, level), block);
      float expected = vanillaSpeed * ratio;
      if (Math.abs(electricSpeed - expected) > 0.01F) {
        helper.fail(electric + " at efficiency " + level + " breaks at " + electricSpeed + ", expected " + expected
            + " (" + ratio + " x " + vanilla + " at " + vanillaSpeed + ")");
        return;
      }
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void ironDrillMatchesIronPickaxe(GameTestHelper helper) {
    assertRatio(helper, ModItems.MINING_DRILL, Items.IRON_PICKAXE, STONE, 1.0F);
  }

  @GameTest(template = TEMPLATE)
  public static void diamondDrillFivePercentOverDiamondPickaxe(GameTestHelper helper) {
    assertRatio(helper, ModItems.DIAMOND_DRILL, Items.DIAMOND_PICKAXE, STONE, 1.05F);
  }

  @GameTest(template = TEMPLATE)
  public static void iridiumDrillTwentyPercentOverNetheritePickaxe(GameTestHelper helper) {
    assertRatio(helper, ModItems.IRIDIUM_DRILL, Items.NETHERITE_PICKAXE, STONE, 1.2F);
  }

  @GameTest(template = TEMPLATE)
  public static void chainsawMatchesIronAxe(GameTestHelper helper) {
    assertRatio(helper, ModItems.CHAINSAW, Items.IRON_AXE, LOG, 1.0F);
  }

  @GameTest(template = TEMPLATE)
  public static void diamondChainsawFivePercentOverDiamondAxe(GameTestHelper helper) {
    assertRatio(helper, ModItems.DIAMOND_CHAINSAW, Items.DIAMOND_AXE, LOG, 1.05F);
  }

  @GameTest(template = TEMPLATE)
  public static void iridiumChainsawTwentyPercentOverNetheriteAxe(GameTestHelper helper) {
    assertRatio(helper, ModItems.IRIDIUM_CHAINSAW, Items.NETHERITE_AXE, LOG, 1.2F);
  }

  @GameTest(template = TEMPLATE)
  public static void chainsawDigsStoneByHand(GameTestHelper helper) {
    ItemStack chainsaw = ((ElectricItem) ModItems.IRIDIUM_CHAINSAW).makeFullStack();
    if (chainsaw.getDestroySpeed(STONE) != 1.0F) {
      helper.fail("iridium chainsaw breaks stone at " + chainsaw.getDestroySpeed(STONE) + ", expected 1.0");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void dischargedDrillDigsByHand(GameTestHelper helper) {
    ItemStack drill = new ItemStack(ModItems.IRIDIUM_DRILL);
    drill.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
    if (drill.getDestroySpeed(STONE) != 1.0F) {
      helper.fail("discharged iridium drill breaks at " + drill.getDestroySpeed(STONE) + ", expected 1.0");
      return;
    }
    helper.succeed();
  }
}
