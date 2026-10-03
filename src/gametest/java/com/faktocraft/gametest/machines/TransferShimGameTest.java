package com.faktocraft.gametest.machines;

import com.faktocraft.common.block.impl.quarry.BlockEntityGantry;
import com.faktocraft.common.block.impl.quarry.QuarryRegistry;
import com.faktocraft.common.util.ItemStackHandler;
import com.faktocraft.common.util.transfer.InvWrapper;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public class TransferShimGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos QUARRY = new BlockPos(2, 2, 2);
  private static final BlockPos HOPPER = new BlockPos(2, 1, 2);
  private static final int GRAVEL = 16;

  @GameTest(template = TEMPLATE, timeoutTicks = 120)
  public static void hopperAbortKeepsGantryItems(GameTestHelper helper) {
    helper.setBlock(HOPPER, Blocks.HOPPER.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, HOPPER) instanceof HopperBlockEntity hopper)) {
      helper.fail("no hopper block entity");
      return;
    }
    for (int i = 0; i < hopper.getContainerSize(); i++) {
      hopper.setItem(i, new ItemStack(Items.COBBLESTONE, 63));
    }
    helper.setBlock(QUARRY, QuarryRegistry.QUARRY.defaultBlockState());
    BlockPos abs = helper.absolutePos(QUARRY);
    QuarryRegistry.QUARRY.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null,
        ItemStack.EMPTY);
    if (!(TestUtil.blockEntity(helper, QUARRY) instanceof BlockEntityGantry gantry)) {
      helper.fail("no quarry block entity");
      return;
    }
    gantry.getInventory().setStackInSlot(0, new ItemStack(Items.GRAVEL, GRAVEL));
    helper.runAfterDelay(60, () -> {
      ItemStack kept = gantry.getInventory().getStackInSlot(0);
      if (!kept.is(Items.GRAVEL) || kept.getCount() != GRAVEL) {
        helper.fail("gantry lost items to an aborted hopper transaction: " + kept);
        return;
      }
      for (int i = 0; i < hopper.getContainerSize(); i++) {
        ItemStack stack = hopper.getItem(i);
        if (!stack.is(Items.COBBLESTONE) || stack.getCount() != 63) {
          helper.fail("hopper contents changed: " + stack);
          return;
        }
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE)
  public static void invWrapperHonoursSlotLimit(GameTestHelper helper) {
    ItemStackHandler handler = new ItemStackHandler(1);
    InvWrapper wrapper = new InvWrapper(handler) {
      @Override
      public int getSlotLimit(int slot) {
        return 1;
      }
    };
    ItemStack remainder = wrapper.insertItem(0, new ItemStack(Items.COBBLESTONE, 2), false);
    if (remainder.getCount() != 1 || handler.getStackInSlot(0).getCount() != 1) {
      helper.fail("slot limit ignored: remainder=" + remainder + " slot=" + handler.getStackInSlot(0));
      return;
    }
    ItemStack refused = wrapper.insertItem(0, new ItemStack(Items.COBBLESTONE, 1), false);
    if (refused.getCount() != 1 || handler.getStackInSlot(0).getCount() != 1) {
      helper.fail("full slot accepted more: remainder=" + refused + " slot=" + handler.getStackInSlot(0));
      return;
    }
    ItemStack simulated = wrapper.insertItem(0, new ItemStack(Items.COBBLESTONE, 1), true);
    if (simulated.getCount() != 1) {
      helper.fail("simulate disagreed with execute: " + simulated);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void invWrapperNotifiesWhenStacking(GameTestHelper helper) {
    AtomicInteger changes = new AtomicInteger();
    ItemStackHandler handler = new ItemStackHandler(1) {
      @Override
      protected void onContentsChanged(int slot) {
        changes.incrementAndGet();
      }
    };
    handler.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 3));
    changes.set(0);
    InvWrapper wrapper = new InvWrapper(handler);
    ItemStack remainder = wrapper.insertItem(0, new ItemStack(Items.COBBLESTONE, 2), false);
    if (!remainder.isEmpty() || handler.getStackInSlot(0).getCount() != 5) {
      helper.fail("stacking insert failed: remainder=" + remainder + " slot=" + handler.getStackInSlot(0));
      return;
    }
    if (changes.get() == 0) {
      helper.fail("onContentsChanged was not called when stacking onto an existing stack");
      return;
    }
    ItemStack simulated = wrapper.insertItem(0, new ItemStack(Items.COBBLESTONE, 70), true);
    if (simulated.getCount() != 11 || handler.getStackInSlot(0).getCount() != 5) {
      helper.fail("simulate changed the slot or miscounted: " + simulated + " slot=" + handler.getStackInSlot(0));
      return;
    }
    helper.succeed();
  }
}
