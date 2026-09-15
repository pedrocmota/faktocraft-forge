package com.faktocraft.gametest.machines;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.FluidCellTankHelper;
import com.faktocraft.common.block.impl.machines.canning_machine.BlockEntityCanningMachine;
import com.faktocraft.common.enums.CanningMachineMode;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class CanningMachineGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos POS = new BlockPos(1, 1, 1);

  private static BlockEntityCanningMachine place(GameTestHelper helper, Fluid fluid, int amount) {
    helper.setBlock(POS, M3Registry.CANNING_MACHINE.defaultBlockState());
    if (!(helper.getBlockEntity(POS) instanceof BlockEntityCanningMachine machine)) {
      throw new GameTestAssertException("no canning machine block entity");
    }
    machine.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.BASIC_CAPACITOR));
    machine.getEnergyStorage().setEnergy(machine.getEnergyStorage().maxEnergy());
    machine.fluidStorage.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
    return machine;
  }

  private static boolean isWaterBottle(ItemStack stack) {
    return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void canningMachineFillsGlassBottles(GameTestHelper helper) {
    BlockEntityCanningMachine machine = place(helper, Fluids.WATER, 1000);
    machine.getItemStackHandler().setStackInSlot(BlockEntityCanningMachine.CELL_UP,
        new ItemStack(Items.GLASS_BOTTLE, 2));
    int[] collected = { 0 };
    helper.succeedWhen(() -> {
      machine.getEnergyStorage().setEnergy(machine.getEnergyStorage().maxEnergy());
      ItemStack output = machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_DOWN);
      if (!output.isEmpty()) {
        if (!isWaterBottle(output) || output.getCount() != 1) {
          throw new GameTestAssertException("expected one water bottle, got " + output);
        }
        machine.getItemStackHandler().setStackInSlot(BlockEntityCanningMachine.CELL_DOWN, ItemStack.EMPTY);
        collected[0]++;
      }
      if (collected[0] < 2) {
        helper.fail("collected " + collected[0] + " water bottles so far");
      }
      if (!machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_UP).isEmpty()) {
        helper.fail("glass bottles were not consumed");
      }
      int expected = 1000 - 2 * FluidCellTankHelper.BOTTLE_MB;
      if (machine.fluidStorage.getFluidAmount() != expected) {
        throw new GameTestAssertException("tank should hold " + expected + " mB, holds "
            + machine.fluidStorage.getFluidAmount());
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void canningMachineLeavesBottleWithoutWater(GameTestHelper helper) {
    BlockEntityCanningMachine machine = place(helper, Fluids.LAVA, 1000);
    if (!machine.isItemValidForSlot(BlockEntityCanningMachine.CELL_UP, new ItemStack(Items.GLASS_BOTTLE))) {
      helper.fail("canning machine rejected a glass bottle");
    }
    machine.getItemStackHandler().setStackInSlot(BlockEntityCanningMachine.CELL_UP,
        new ItemStack(Items.GLASS_BOTTLE));
    helper.runAfterDelay(120, () -> {
      ItemStack up = machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_UP);
      if (!up.is(Items.GLASS_BOTTLE)) {
        helper.fail("glass bottle should stay untouched with lava in the tank, slot holds " + up);
      }
      if (!machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_DOWN).isEmpty()) {
        helper.fail("nothing should be produced from lava");
      }
      if (machine.fluidStorage.getFluidAmount() != 1000) {
        helper.fail("lava was drained");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void canningMachineFillsBucketFromTank(GameTestHelper helper) {
    BlockEntityCanningMachine machine = place(helper, Fluids.WATER, 1000);
    if (!machine.isItemValidForSlot(BlockEntityCanningMachine.CELL_UP, new ItemStack(Items.BUCKET))) {
      helper.fail("canning machine rejected an empty bucket");
    }
    machine.getItemStackHandler().setStackInSlot(BlockEntityCanningMachine.CELL_UP, new ItemStack(Items.BUCKET));
    helper.succeedWhen(() -> {
      machine.getEnergyStorage().setEnergy(machine.getEnergyStorage().maxEnergy());
      ItemStack output = machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_DOWN);
      if (!output.is(Items.WATER_BUCKET)) {
        helper.fail("expected a water bucket, got " + output);
      }
      if (!machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_UP).isEmpty()) {
        throw new GameTestAssertException("empty bucket was not consumed");
      }
      if (machine.fluidStorage.getFluidAmount() != 0) {
        throw new GameTestAssertException("tank should be empty, holds " + machine.fluidStorage.getFluidAmount());
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void canningMachineEmptiesLavaBucketIntoTank(GameTestHelper helper) {
    BlockEntityCanningMachine machine = place(helper, Fluids.LAVA, 0);
    machine.changeMode();
    if (machine.getMode() != CanningMachineMode.EMPTY) {
      helper.fail("canning machine should be in empty mode");
    }
    machine.getItemStackHandler().setStackInSlot(BlockEntityCanningMachine.CELL_UP,
        new ItemStack(Items.LAVA_BUCKET));
    helper.succeedWhen(() -> {
      machine.getEnergyStorage().setEnergy(machine.getEnergyStorage().maxEnergy());
      ItemStack output = machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_DOWN);
      if (!output.is(Items.BUCKET)) {
        helper.fail("expected an empty bucket, got " + output);
      }
      if (machine.fluidStorage.getFluid() != Fluids.LAVA || machine.fluidStorage.getFluidAmount() != 1000) {
        throw new GameTestAssertException("tank should hold 1000 mB of lava, holds "
            + machine.fluidStorage.getFluidAmount() + " of " + machine.fluidStorage.getFluid());
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void canningMachineKeepsBucketWithHalfTank(GameTestHelper helper) {
    BlockEntityCanningMachine machine = place(helper, Fluids.WATER, 500);
    machine.getItemStackHandler().setStackInSlot(BlockEntityCanningMachine.CELL_UP, new ItemStack(Items.BUCKET));
    helper.runAfterDelay(120, () -> {
      ItemStack up = machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_UP);
      if (!up.is(Items.BUCKET)) {
        helper.fail("bucket should stay untouched with half a bucket in the tank, slot holds " + up);
      }
      if (!machine.getItemStackHandler().getStackInSlot(BlockEntityCanningMachine.CELL_DOWN).isEmpty()) {
        helper.fail("nothing should be produced from 500 mB");
      }
      if (machine.fluidStorage.getFluidAmount() != 500) {
        helper.fail("water was drained");
      }
      helper.succeed();
    });
  }
}
