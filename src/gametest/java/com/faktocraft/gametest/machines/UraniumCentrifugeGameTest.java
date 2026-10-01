package com.faktocraft.gametest.machines;

import com.faktocraft.common.block.impl.machines.uranium_centrifuge.BlockEntityUraniumCentrifuge;
import com.faktocraft.common.entity.block.BlockEntityStandardMachine;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import com.faktocraft.common.util.transfer.ForgeCapabilities;

public class UraniumCentrifugeGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos POS = new BlockPos(1, 1, 1);

  private static BlockEntityUraniumCentrifuge place(GameTestHelper helper) {
    helper.setBlock(POS, M3Registry.URANIUM_CENTRIFUGE.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, POS) instanceof BlockEntityUraniumCentrifuge centrifuge)) {
      throw TestUtil.assertion(helper, "no uranium centrifuge block entity");
    }
    centrifuge.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.BASIC_CAPACITOR));
    centrifuge.getEnergyStorage().setEnergy(centrifuge.getEnergyStorage().maxEnergy());
    return centrifuge;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void uraniumCentrifugeProducesDepletedDust(GameTestHelper helper) {
    BlockEntityUraniumCentrifuge centrifuge = place(helper);
    centrifuge.getItemStackHandler().setStackInSlot(BlockEntityStandardMachine.INPUT_SLOT,
        new ItemStack(ModItems.URANIUM_DUST, 4));
    helper.succeedWhen(() -> {
      centrifuge.getEnergyStorage().setEnergy(centrifuge.getEnergyStorage().maxEnergy());
      ItemStack output = centrifuge.getItemStackHandler().getStackInSlot(BlockEntityStandardMachine.OUTPUT_SLOT);
      if (!output.is(ModItems.DEPLETED_URANIUM_DUST)) {
        helper.fail("no depleted uranium dust yet");
      }
      ItemStack bonus = centrifuge.getItemStackHandler().getStackInSlot(BlockEntityStandardMachine.BONUS_SLOT);
      if (!bonus.isEmpty() && !bonus.is(ModItems.ENRICHED_URANIUM_DUST)) {
        helper.fail("bonus slot holds something other than enriched uranium dust");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void uraniumCentrifugeReprocessesDepletedRod(GameTestHelper helper) {
    BlockEntityUraniumCentrifuge centrifuge = place(helper);
    centrifuge.getItemStackHandler().setStackInSlot(BlockEntityStandardMachine.INPUT_SLOT,
        new ItemStack(ModItems.DEPLETED_FUEL_ROD));
    helper.succeedWhen(() -> {
      centrifuge.getEnergyStorage().setEnergy(centrifuge.getEnergyStorage().maxEnergy());
      ItemStack output = centrifuge.getItemStackHandler().getStackInSlot(BlockEntityStandardMachine.OUTPUT_SLOT);
      if (!output.is(ModItems.EMPTY_FUEL_ROD)) {
        helper.fail("no empty rod came back yet");
      }
      ItemStack bonus = centrifuge.getItemStackHandler().getStackInSlot(BlockEntityStandardMachine.BONUS_SLOT);
      if (!bonus.is(ModItems.ENRICHED_URANIUM_DUST) && !bonus.is(ModItems.NUCLEAR_WASTE)
          && !bonus.is(ModItems.PLUTONIUM)) {
        helper.fail("bonus slot should hold enriched dust, nuclear waste or plutonium, holds " + bonus);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void uraniumCentrifugeOnlyExposesItemsBelow(GameTestHelper helper) {
    BlockEntityUraniumCentrifuge centrifuge = place(helper);
    for (Direction side : Direction.values()) {
      boolean present = centrifuge.getCapability(ForgeCapabilities.ITEM_HANDLER, side).isPresent();
      if (present != (side == Direction.DOWN)) {
        helper.fail("item handler on side " + side + " should be " + (side == Direction.DOWN));
      }
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void uraniumCentrifugeRejectsOtherDust(GameTestHelper helper) {
    BlockEntityUraniumCentrifuge centrifuge = place(helper);
    if (centrifuge.isItemValidForSlot(BlockEntityStandardMachine.INPUT_SLOT, new ItemStack(ModItems.COAL_DUST))) {
      helper.fail("centrifuge accepted coal dust");
    }
    if (!centrifuge.isItemValidForSlot(BlockEntityStandardMachine.INPUT_SLOT,
        new ItemStack(ModItems.URANIUM_DUST))) {
      helper.fail("centrifuge refused uranium dust");
    }
    helper.succeed();
  }
}
