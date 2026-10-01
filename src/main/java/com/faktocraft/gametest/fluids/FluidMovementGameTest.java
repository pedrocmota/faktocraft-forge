package com.faktocraft.gametest.fluids;

import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.gametest.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class FluidMovementGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final BlockPos POOL_MIN = new BlockPos(1, 1, 1);
  private static final BlockPos POOL_MAX = new BlockPos(3, 3, 3);
  private static final BlockPos SPAWN = new BlockPos(2, 3, 2);

  private static void buildPool(GameTestHelper helper, net.minecraft.world.level.block.Block fluidBlock) {
    for (int x = POOL_MIN.getX() - 1; x <= POOL_MAX.getX() + 1; x++) {
      for (int z = POOL_MIN.getZ() - 1; z <= POOL_MAX.getZ() + 1; z++) {
        for (int y = POOL_MIN.getY(); y <= POOL_MAX.getY() + 1; y++) {
          boolean inside = x >= POOL_MIN.getX() && x <= POOL_MAX.getX()
              && z >= POOL_MIN.getZ() && z <= POOL_MAX.getZ() && y <= POOL_MAX.getY();
          helper.setBlock(new BlockPos(x, y, z),
              inside ? fluidBlock.defaultBlockState() : Blocks.STONE.defaultBlockState());
        }
      }
    }
  }

  private static void assertMoves(GameTestHelper helper, net.minecraft.world.level.block.Block fluidBlock,
      String name) {
    buildPool(helper, fluidBlock);
    Pig pig = helper.spawn(EntityTypes.PIG, SPAWN);
    Vec3 start = pig.position();
    helper.runAfterDelay(20, () -> {
      if (!pig.isAlive()) {
        helper.fail("pig died inside " + name);
        return;
      }
      if (!pig.isInFluidType()) {
        helper.fail("pig is not inside " + name);
        return;
      }
      if (pig.position().distanceTo(start) < 0.05) {
        helper.fail("pig is frozen inside " + name + " (no movement in 20 ticks)");
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void livingEntityMovesInOil(GameTestHelper helper) {
    assertMoves(helper, ModFluids.OIL.block(), "oil");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void livingEntityMovesInCoolant(GameTestHelper helper) {
    assertMoves(helper, ModFluids.COOLANT.block(), "coolant");
  }
}
