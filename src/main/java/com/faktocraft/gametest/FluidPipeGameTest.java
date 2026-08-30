package com.faktocraft.gametest;

import com.faktocraft.IndReb;
import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.faktocraft.common.block.impl.pipe.PipeValve;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@net.minecraftforge.gametest.GameTestHolder(IndReb.MODID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class FluidPipeGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static void placePipe(GameTestHelper helper, BlockPos rel, Direction... connections) {
    BlockState state = PipeRegistry.FLUID_STONE_PIPE.defaultBlockState();
    for (Direction direction : connections) {
      state = state.setValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction), true);
    }
    helper.setBlock(rel, state);
  }

  private static void prefill(GameTestHelper helper, BlockPos rel, int mb) {
    if (helper.getBlockEntity(rel) instanceof BlockEntityFluidPipe pipe) {
      pipe.tank.fillFluid(new FluidStack(Fluids.WATER, mb), mb, false);
    }
  }

  private static void succeedWhenFullyDrained(GameTestHelper helper, BlockPos tankPos, int expected,
      BlockPos... pipes) {
    helper.succeedWhen(() -> {
      StringBuilder status = new StringBuilder();
      int tankAmount = helper.getBlockEntity(tankPos) instanceof BlockEntityTank tank
          ? tank.tank.getFluidAmount()
          : -1;
      status.append("tank=").append(tankAmount).append('/').append(expected);
      boolean pipesEmpty = true;
      for (BlockPos pipePos : pipes) {
        int left = helper.getBlockEntity(pipePos) instanceof BlockEntityFluidPipe pipe
            ? pipe.tank.getFluidAmount()
            : -1;
        if (left != 0) {
          pipesEmpty = false;
        }
        status.append(" pipe@").append(pipePos.toShortString()).append('=').append(left);
      }
      if (tankAmount != expected || !pipesEmpty) {
        helper.fail("fluid stranded: " + status);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void tailDrainsIntoTankEast(GameTestHelper helper) {
    BlockPos tank = new BlockPos(6, 1, 1);
    helper.setBlock(tank, PipeRegistry.TANK.defaultBlockState());
    placePipe(helper, new BlockPos(2, 1, 1), Direction.EAST);
    placePipe(helper, new BlockPos(3, 1, 1), Direction.WEST, Direction.EAST);
    placePipe(helper, new BlockPos(4, 1, 1), Direction.WEST, Direction.EAST);
    placePipe(helper, new BlockPos(5, 1, 1), Direction.WEST, Direction.EAST);
    prefill(helper, new BlockPos(2, 1, 1), 100);
    succeedWhenFullyDrained(helper, tank, 100,
        new BlockPos(2, 1, 1), new BlockPos(3, 1, 1), new BlockPos(4, 1, 1), new BlockPos(5, 1, 1));
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void tailDrainsIntoTankWest(GameTestHelper helper) {
    BlockPos tank = new BlockPos(1, 1, 1);
    helper.setBlock(tank, PipeRegistry.TANK.defaultBlockState());
    placePipe(helper, new BlockPos(2, 1, 1), Direction.WEST, Direction.EAST);
    placePipe(helper, new BlockPos(3, 1, 1), Direction.WEST, Direction.EAST);
    placePipe(helper, new BlockPos(4, 1, 1), Direction.WEST, Direction.EAST);
    placePipe(helper, new BlockPos(5, 1, 1), Direction.WEST);
    prefill(helper, new BlockPos(5, 1, 1), 100);
    succeedWhenFullyDrained(helper, tank, 100,
        new BlockPos(2, 1, 1), new BlockPos(3, 1, 1), new BlockPos(4, 1, 1), new BlockPos(5, 1, 1));
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void plateauDrainsIntoTank(GameTestHelper helper) {
    BlockPos tank = new BlockPos(6, 1, 1);
    helper.setBlock(tank, PipeRegistry.TANK.defaultBlockState());
    placePipe(helper, new BlockPos(2, 1, 1), Direction.EAST);
    placePipe(helper, new BlockPos(3, 1, 1), Direction.WEST, Direction.EAST);
    placePipe(helper, new BlockPos(4, 1, 1), Direction.WEST, Direction.EAST);
    placePipe(helper, new BlockPos(5, 1, 1), Direction.WEST, Direction.EAST);
    for (int x = 2; x <= 5; x++) {
      prefill(helper, new BlockPos(x, 1, 1), 50);
    }
    succeedWhenFullyDrained(helper, tank, 200,
        new BlockPos(2, 1, 1), new BlockPos(3, 1, 1), new BlockPos(4, 1, 1), new BlockPos(5, 1, 1));
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void valveReleaseStreamStaysVisible(GameTestHelper helper) {
    BlockPos valvePos = new BlockPos(2, 1, 1);
    BlockPos middle = new BlockPos(3, 1, 1);
    BlockPos tankPos = new BlockPos(4, 1, 1);
    placePipe(helper, valvePos, Direction.WEST, Direction.EAST);
    placePipe(helper, middle, Direction.WEST, Direction.EAST);
    helper.setBlock(tankPos, PipeRegistry.TANK.defaultBlockState());
    prefill(helper, valvePos, 200);
    BlockEntityFluidPipe valvePipe = (BlockEntityFluidPipe) helper.getBlockEntity(valvePos);
    valvePipe.setValve(PipeValve.of(Direction.NORTH, false));
    java.util.concurrent.atomic.AtomicBoolean middleSeen = new java.util.concurrent.atomic.AtomicBoolean();
    helper.runAfterDelay(5, () -> valvePipe.setValve(PipeValve.of(Direction.NORTH, true)));
    helper.succeedWhen(() -> {
      if (helper.getBlockEntity(middle) instanceof BlockEntityFluidPipe pipe
          && pipe.tank.getFluidAmount() > 0) {
        middleSeen.set(true);
      }
      int tankAmount = helper.getBlockEntity(tankPos) instanceof BlockEntityTank tank
          ? tank.tank.getFluidAmount()
          : -1;
      if (tankAmount != 200) {
        helper.fail("tank=" + tankAmount + "/200");
      }
      if (!middleSeen.get()) {
        helper.fail("stream teleported through the pipe next to the tank");
      }
    });
  }

  private static int pipeAmount(GameTestHelper helper, BlockPos rel) {
    return helper.getBlockEntity(rel) instanceof BlockEntityFluidPipe pipe ? pipe.tank.getFluidAmount() : -1;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void closedValveIsolatesPipe(GameTestHelper helper) {
    BlockPos a = new BlockPos(2, 1, 1);
    BlockPos b = new BlockPos(3, 1, 1);
    BlockPos tankPos = new BlockPos(4, 1, 1);
    placePipe(helper, a, Direction.WEST, Direction.EAST);
    placePipe(helper, b, Direction.WEST, Direction.EAST);
    helper.setBlock(tankPos, PipeRegistry.TANK.defaultBlockState());
    prefill(helper, a, 200);
    ((BlockEntityFluidPipe) helper.getBlockEntity(b)).setValve(PipeValve.of(Direction.NORTH, false));
    helper.runAfterDelay(40, () -> {
      int inA = pipeAmount(helper, a);
      int inB = pipeAmount(helper, b);
      int inTank = helper.getBlockEntity(tankPos) instanceof BlockEntityTank tank
          ? tank.tank.getFluidAmount()
          : -1;
      if (inA != 200 || inB != 0 || inTank != 0) {
        helper.fail("closed valve leaked: a=" + inA + " b=" + inB + " tank=" + inTank);
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void junctionRespectsTierRate(GameTestHelper helper) {
    BlockPos center = new BlockPos(3, 1, 2);
    placePipe(helper, center, Direction.WEST, Direction.EAST, Direction.SOUTH);
    placePipe(helper, new BlockPos(2, 1, 2), Direction.EAST);
    placePipe(helper, new BlockPos(4, 1, 2), Direction.WEST);
    placePipe(helper, new BlockPos(3, 1, 3), Direction.NORTH);
    prefill(helper, center, 200);
    java.util.concurrent.atomic.AtomicInteger previous = new java.util.concurrent.atomic.AtomicInteger(200);
    java.util.concurrent.atomic.AtomicInteger worstMove = new java.util.concurrent.atomic.AtomicInteger();
    helper.succeedWhen(() -> {
      int now = pipeAmount(helper, center);
      int moved = previous.getAndSet(now) - now;
      worstMove.getAndAccumulate(moved, Math::max);
      if (worstMove.get() > 50) {
        helper.fail("junction moved " + worstMove.get() + " mB in one tick (rate is 50)");
      }
      if (now > 50) {
        helper.fail("center still above equalization level: " + now);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void columnDrainsIntoTankBelow(GameTestHelper helper) {
    BlockPos tank = new BlockPos(2, 1, 1);
    helper.setBlock(tank, PipeRegistry.TANK.defaultBlockState());
    placePipe(helper, new BlockPos(2, 2, 1), Direction.DOWN, Direction.UP);
    placePipe(helper, new BlockPos(2, 3, 1), Direction.DOWN);
    prefill(helper, new BlockPos(2, 3, 1), 100);
    succeedWhenFullyDrained(helper, tank, 100, new BlockPos(2, 2, 1), new BlockPos(2, 3, 1));
  }
}
