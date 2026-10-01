package com.faktocraft.gametest.blocks;

import com.faktocraft.common.block.BlockIridiumOre;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.gametest.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;

public class IridiumOreGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos ORE = new BlockPos(2, 2, 2);

  private static void assertFaintLight(GameTestHelper helper, Block ore) {
    helper.setBlock(ORE, ore.defaultBlockState());
    BlockPos orePos = helper.absolutePos(ORE);
    int emission = ore.defaultBlockState().getLightEmission(helper.getLevel(), orePos);
    if (emission != BlockIridiumOre.LIGHT_LEVEL) {
      helper.fail(ore + " emits " + emission + ", expected " + BlockIridiumOre.LIGHT_LEVEL);
      return;
    }
    helper.runAfterDelay(5, () -> {
      BlockPos beside = helper.absolutePos(ORE.east());
      int light = helper.getLevel().getBrightness(LightLayer.BLOCK, beside);
      if (light != BlockIridiumOre.LIGHT_LEVEL - 1) {
        helper.fail("block light beside " + ore + " is " + light + ", expected " + (BlockIridiumOre.LIGHT_LEVEL - 1));
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void iridiumOreEmitsFaintLight(GameTestHelper helper) {
    assertFaintLight(helper, ModBlocks.IRIDIUM_ORE);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void deepslateIridiumOreEmitsFaintLight(GameTestHelper helper) {
    assertFaintLight(helper, ModBlocks.DEEPSLATE_IRIDIUM_ORE);
  }
}
