package com.faktocraft.gametest.tools;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.impl.tools.VeinMining;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.List;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class VeinMiningGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static List<BlockPos> collect(GameTestHelper helper, BlockPos origin, int limit, boolean logs) {
    BlockPos abs = helper.absolutePos(origin);
    return VeinMining.collect(helper.getLevel(), abs, helper.getLevel().getBlockState(abs),
        logs ? VeinMining.LOGS : VeinMining.ORES, limit);
  }

  private static boolean contains(GameTestHelper helper, List<BlockPos> vein, BlockPos rel) {
    return vein.contains(helper.absolutePos(rel));
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void oreVeinGroupsDeepslateVariantAndSkipsOtherOres(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), Blocks.IRON_ORE);
    helper.setBlock(new BlockPos(2, 1, 1), Blocks.IRON_ORE);
    helper.setBlock(new BlockPos(2, 2, 1), Blocks.IRON_ORE);
    helper.setBlock(new BlockPos(3, 2, 2), Blocks.DEEPSLATE_IRON_ORE);
    helper.setBlock(new BlockPos(1, 2, 1), Blocks.COAL_ORE);
    helper.setBlock(new BlockPos(4, 3, 3), Blocks.RAW_IRON_BLOCK);
    List<BlockPos> vein = collect(helper, new BlockPos(1, 1, 1), 64, false);
    if (vein.size() != 3 || contains(helper, vein, new BlockPos(1, 2, 1))
        || !contains(helper, vein, new BlockPos(3, 2, 2)) || contains(helper, vein, new BlockPos(4, 3, 3))) {
      helper.fail("vein was " + vein.size() + " extra blocks: " + vein);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void veinStopsAtTheConfiguredLimit(GameTestHelper helper) {
    for (int x = 1; x <= 8; x++) {
      helper.setBlock(new BlockPos(x, 1, 1), Blocks.COAL_ORE);
    }
    List<BlockPos> vein = collect(helper, new BlockPos(1, 1, 1), 4, false);
    if (vein.size() != 3) {
      helper.fail("limit 4 produced " + vein.size() + " extra blocks");
      return;
    }
    if (!collect(helper, new BlockPos(1, 1, 1), 1, false).isEmpty()) {
      helper.fail("limit 1 still produced extra blocks");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void treeVeinOnlyFollowsTheSameLog(GameTestHelper helper) {
    Block oak = Blocks.OAK_LOG;
    helper.setBlock(new BlockPos(2, 1, 2), oak);
    helper.setBlock(new BlockPos(2, 2, 2), oak);
    helper.setBlock(new BlockPos(2, 3, 2), oak);
    helper.setBlock(new BlockPos(3, 4, 3), oak);
    helper.setBlock(new BlockPos(1, 2, 2), Blocks.BIRCH_LOG);
    helper.setBlock(new BlockPos(2, 4, 2), Blocks.OAK_LEAVES);
    List<BlockPos> vein = collect(helper, new BlockPos(2, 1, 2), 64, true);
    if (vein.size() != 3 || contains(helper, vein, new BlockPos(1, 2, 2))
        || contains(helper, vein, new BlockPos(2, 4, 2))) {
      helper.fail("tree vein was " + vein.size() + " extra logs: " + vein);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void glowstoneCountsAsVein(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), Blocks.GLOWSTONE);
    helper.setBlock(new BlockPos(1, 1, 2), Blocks.GLOWSTONE);
    helper.setBlock(new BlockPos(2, 2, 3), Blocks.GLOWSTONE);
    List<BlockPos> vein = collect(helper, new BlockPos(1, 1, 1), 64, false);
    if (vein.size() != 2) {
      helper.fail("glowstone vein was " + vein.size() + " extra blocks");
      return;
    }
    helper.succeed();
  }
}
