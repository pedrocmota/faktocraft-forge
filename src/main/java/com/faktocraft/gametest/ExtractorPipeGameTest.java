package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidExtractorPipe;
import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.faktocraft.common.block.impl.pipe.BlockFluidExtractorPipe;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

@net.minecraftforge.gametest.GameTestHolder(Faktocraft.MODID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class ExtractorPipeGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static void energize(com.faktocraft.common.block.impl.pipe.PipeExtractor extractor) {
    extractor.getDock().setStackInSlot(0,
        new ItemStack(com.faktocraft.common.registries.ModItems.CRUDE_CAPACITOR));
    extractor.energy().setEnergy(1000);
  }

  private static BlockPos buildFluidLine(GameTestHelper helper, boolean powered) {
    BlockPos pipe = new BlockPos(2, 1, 1);
    helper.setBlock(pipe, PipeRegistry.FLUID_EXTRACTOR_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(1, 1, 1), PipeRegistry.TANK.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), PipeRegistry.TANK.defaultBlockState());
    if (helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof BlockEntityTank tank) {
      tank.tank.fillFluid(new net.minecraftforge.fluids.FluidStack(
          net.minecraft.world.level.material.Fluids.WATER, 4000), 4000, false);
    }
    BlockPos abs = helper.absolutePos(pipe);
    helper.getLevel().setBlockAndUpdate(abs,
        helper.getLevel().getBlockState(abs).setValue(BlockFluidExtractorPipe.SOURCE, Direction.WEST));
    if (powered && helper.getBlockEntity(pipe) instanceof BlockEntityFluidExtractorPipe extractor) {
      energize(extractor.extractor());
    }
    return pipe;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void fluidExtractorPipeDraftsFromItsTank(GameTestHelper helper) {
    buildFluidLine(helper, true);
    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(3, 1, 1)) instanceof BlockEntityTank dest)
          || dest.tank.getFluidAmount() < 50) {
        helper.fail("no water reached the far tank");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void fluidExtractorPipeIdlesWithoutPower(GameTestHelper helper) {
    buildFluidLine(helper, false);
    helper.runAfterDelay(120, () -> {
      if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof BlockEntityTank tank)
          || tank.tank.getFluidAmount() != 4000) {
        helper.fail("the pipe drafted with an empty buffer");
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void fluidExtractorPipeAnswersTheEnergyLookup(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), PipeRegistry.FLUID_EXTRACTOR_PIPE.defaultBlockState());
    if (!com.faktocraft.common.energy.EnergyLookup.isPresent(helper.getLevel(),
        helper.absolutePos(new BlockPos(1, 1, 1)), null)) {
      helper.fail("cables cannot see the fluid extractor pipe");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void extractorPipeBufferIsTheDockedCapacitor(GameTestHelper helper) {
    BlockPos pipe = new BlockPos(2, 1, 1);
    helper.setBlock(pipe, PipeRegistry.FLUID_EXTRACTOR_PIPE.defaultBlockState());
    if (!(helper.getBlockEntity(pipe) instanceof BlockEntityFluidExtractorPipe extractor)) {
      helper.fail("no fluid extractor pipe");
      return;
    }
    var motor = extractor.extractor();
    motor.energy().setEnergy(1000);
    if (motor.energy().energyStored() != 0) {
      helper.fail("the empty dock still buffered energy");
      return;
    }
    motor.getDock().setStackInSlot(0,
        new ItemStack(com.faktocraft.common.registries.ModItems.CRUDE_CAPACITOR));
    if (motor.energy().maxEnergy() != 1000) {
      helper.fail("the crude capacitor did not set the buffer to 1000, got "
          + motor.energy().maxEnergy());
      return;
    }
    motor.getDock().setStackInSlot(1,
        new ItemStack(com.faktocraft.common.registries.ModItems.CRUDE_CAPACITOR));
    if (motor.energy().maxEnergy() != 2000) {
      helper.fail("two crude capacitors did not sum to 2000, got " + motor.energy().maxEnergy());
      return;
    }
    motor.getDock().setStackInSlot(1, ItemStack.EMPTY);
    motor.energy().setEnergy(1000);
    motor.getDock().setStackInSlot(0, ItemStack.EMPTY);
    if (motor.energy().maxEnergy() != 0 || motor.energy().energyStored() != 0) {
      helper.fail("removing the capacitor left buffer=" + motor.energy().maxEnergy()
          + " stored=" + motor.energy().energyStored());
      return;
    }
    helper.succeed();
  }
}
