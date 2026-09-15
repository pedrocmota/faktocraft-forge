package com.faktocraft.gametest.fluids;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.pipe.BlockEntityEnderTank;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

@net.minecraftforge.gametest.GameTestHolder(Faktocraft.MODID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class EnderTankGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static BlockEntityEnderTank place(GameTestHelper helper, BlockPos rel, int code) {
    helper.setBlock(rel, PipeRegistry.ENDER_TANK.defaultBlockState());
    if (!(helper.getBlockEntity(rel) instanceof BlockEntityEnderTank tank)) {
      throw new GameTestAssertException("no ender tank at " + rel.toShortString());
    }
    tank.setCode(code);
    return tank;
  }

  private static IFluidHandler handler(GameTestHelper helper, BlockEntityEnderTank tank) {
    return tank.getCapability(ForgeCapabilities.FLUID_HANDLER)
        .orElseThrow(() -> new GameTestAssertException("ender tank without fluid handler"));
  }

  private static int amount(GameTestHelper helper, BlockEntityEnderTank tank) {
    return handler(helper, tank).getFluidInTank(0).getAmount();
  }

  private static void drainAll(GameTestHelper helper, BlockEntityEnderTank tank) {
    handler(helper, tank).drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
  }

  @GameTest(template = TEMPLATE)
  public static void enderTanksWithTheSameCodeShareFluid(GameTestHelper helper) {
    BlockEntityEnderTank first = place(helper, new BlockPos(1, 1, 1), 424242);
    BlockEntityEnderTank second = place(helper, new BlockPos(5, 1, 5), 424242);
    int filled = handler(helper, first).fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(filled == 3000, "expected 3000 mB accepted, got " + filled);
    helper.assertTrue(amount(helper, second) == 3000,
        "second tank should see 3000 mB, saw " + amount(helper, second));
    FluidStack drained = handler(helper, second).drain(1000, IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(drained.getAmount() == 1000 && drained.getFluid() == Fluids.WATER,
        "second tank should drain 1000 mB of water");
    helper.assertTrue(amount(helper, first) == 2000, "first tank should see 2000 mB, saw " + amount(helper, first));
    drainAll(helper, first);
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void enderTanksWithDifferentCodesStaySeparate(GameTestHelper helper) {
    BlockEntityEnderTank first = place(helper, new BlockPos(1, 1, 1), 111222);
    BlockEntityEnderTank second = place(helper, new BlockPos(5, 1, 5), 333444);
    handler(helper, first).fill(new FluidStack(Fluids.LAVA, 2000), IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(amount(helper, second) == 0, "second tank should stay empty, saw " + amount(helper, second));
    helper.assertTrue(amount(helper, first) == 2000, "first tank should keep 2000 mB");
    drainAll(helper, first);
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void enderTankWithoutCodeRefusesFluid(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), PipeRegistry.ENDER_TANK.defaultBlockState());
    if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof BlockEntityEnderTank tank)) {
      throw new GameTestAssertException("no ender tank");
    }
    helper.assertFalse(tank.hasCode(), "fresh tank should have no code");
    int filled = handler(helper, tank).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
    helper.assertTrue(filled == 0, "tank without code accepted " + filled + " mB");
    helper.assertTrue(amount(helper, tank) == 0, "tank without code should read empty");
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void enderTankCodeChangeLeavesFluidInTheOldChannel(GameTestHelper helper) {
    BlockEntityEnderTank tank = place(helper, new BlockPos(1, 1, 1), 555000);
    handler(helper, tank).fill(new FluidStack(Fluids.WATER, 2500), IFluidHandler.FluidAction.EXECUTE);
    tank.setCode(555001);
    helper.assertTrue(amount(helper, tank) == 0, "new channel should be empty, saw " + amount(helper, tank));
    tank.setCode(555000);
    helper.assertTrue(amount(helper, tank) == 2500, "old channel should keep 2500 mB, saw " + amount(helper, tank));
    tank.setCode(-1);
    helper.assertFalse(tank.hasCode(), "negative code should clear the tank");
    helper.assertTrue(amount(helper, tank) == 0, "cleared tank should read empty");
    tank.setCode(555000);
    drainAll(helper, tank);
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void stackedEnderTanksDoNotSettleIntoEachOther(GameTestHelper helper) {
    BlockEntityEnderTank lower = place(helper, new BlockPos(1, 1, 1), 777001);
    BlockEntityEnderTank upper = place(helper, new BlockPos(1, 2, 1), 777002);
    handler(helper, upper).fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE);
    helper.runAfterDelay(60, () -> {
      helper.assertTrue(amount(helper, lower) == 0, "lower tank received " + amount(helper, lower) + " mB");
      helper.assertTrue(amount(helper, upper) == 4000, "upper tank lost fluid, has " + amount(helper, upper));
      drainAll(helper, upper);
      helper.succeed();
    });
  }
}
