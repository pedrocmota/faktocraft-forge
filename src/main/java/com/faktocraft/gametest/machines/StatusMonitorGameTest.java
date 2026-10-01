package com.faktocraft.gametest.machines;

import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.BlockStatusMonitor;
import com.faktocraft.common.block.impl.monitor.GenericStatusSources;
import com.faktocraft.common.block.impl.monitor.MonitorRegistry;
import com.faktocraft.common.block.impl.monitor.MonitorCardItem;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.PipeRegistry;
import com.faktocraft.common.registries.machines.M2Registry;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import com.faktocraft.common.util.transfer.CapabilityBridge;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import com.faktocraft.common.util.transfer.IFluidHandler;
import net.neoforged.fml.ModList;

public class StatusMonitorGameTest {
  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos MASTER = new BlockPos(2, 2, 6);
  private static final BlockPos MACHINE = new BlockPos(2, 1, 1);

  private static BlockEntityStatusMonitor panel(GameTestHelper helper, BlockPos master) {
    BlockStatusMonitor block = (BlockStatusMonitor) MonitorRegistry.STATUS_MONITOR;
    block.placePanel(helper.getLevel(), helper.absolutePos(master), Direction.NORTH);
    if (!(TestUtil.blockEntity(helper, master) instanceof BlockEntityStatusMonitor monitor)) {
      throw TestUtil.assertion(helper, "no monitor block entity at the master position");
    }
    return monitor;
  }

  private static int panelBlocks(GameTestHelper helper, BlockPos master) {
    int count = 0;
    for (int x = 0; x < BlockStatusMonitor.WIDTH; x++) {
      for (int y = 0; y < BlockStatusMonitor.HEIGHT; y++) {
        BlockPos rel = master.relative(BlockStatusMonitor.rightOf(Direction.NORTH), x).above(y);
        if (helper.getBlockState(rel).is(MonitorRegistry.STATUS_MONITOR)) {
          count++;
        }
      }
    }
    return count;
  }

  private static void probe(GameTestHelper helper, BlockEntityStatusMonitor monitor, BlockPos rel) {
    ItemStack stack = new ItemStack(MonitorRegistry.MONITOR_CARD);
    MonitorCardItem.copy(stack, helper.getLevel(), helper.absolutePos(rel));
    BlockPos target = MonitorCardItem.targetPos(stack);
    if (target == null || !target.equals(helper.absolutePos(rel))) {
      throw TestUtil.assertion(helper, "probe did not copy the block position");
    }
    monitor.setTarget(MonitorCardItem.targetDimension(stack), target);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void statusMonitorPlacesSixPartsAndBreaksAsOne(GameTestHelper helper) {
    panel(helper, MASTER);
    if (panelBlocks(helper, MASTER) != 6) {
      helper.fail("expected 6 panel blocks, found " + panelBlocks(helper, MASTER));
      return;
    }
    BlockPos part = MASTER.relative(BlockStatusMonitor.rightOf(Direction.NORTH), 2).above(1);
    helper.getLevel().destroyBlock(helper.absolutePos(part), true);
    helper.runAfterDelay(5, () -> {
      if (panelBlocks(helper, MASTER) != 0) {
        helper.fail("panel blocks left after breaking one part: " + panelBlocks(helper, MASTER));
        return;
      }
      helper.assertItemEntityCountIs(MonitorRegistry.STATUS_MONITOR_ITEM, MASTER, 6.0, 1);
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void statusMonitorShowsMachineEnergyAndNoticesRemoval(GameTestHelper helper) {
    helper.setBlock(MACHINE, M2Registry.ELECTRIC_FURNACE.defaultBlockState());
    if (TestUtil.blockEntity(helper, MACHINE) instanceof FaktocraftBlockEntity machine) {
      machine.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.BASIC_CAPACITOR));
      machine.getEnergyStorage().setEnergy(machine.getEnergyStorage().maxEnergy() / 2);
    } else {
      helper.fail("no electric furnace block entity");
      return;
    }
    BlockEntityStatusMonitor monitor = panel(helper, MASTER);
    probe(helper, monitor, MACHINE);
    helper.startSequence()
        .thenWaitUntil(() -> {
          if (monitor.status() != BlockEntityStatusMonitor.STATUS_OK
              || WailaData.maxEnergy(monitor.data()) <= 0) {
            throw TestUtil.assertion(helper, "monitor has no energy data yet, status " + monitor.status());
          }
          if (monitor.data().getIntOr(WailaData.TAG_ENERGY, 0) <= 0) {
            throw TestUtil.assertion(helper, "stored energy was not reported");
          }
        })
        .thenExecute(() -> helper.setBlock(MACHINE, Blocks.AIR.defaultBlockState()))
        .thenWaitUntil(() -> {
          if (monitor.status() != BlockEntityStatusMonitor.STATUS_MISSING) {
            throw TestUtil.assertion(helper, "monitor did not notice the removed block, status "
                + monitor.status());
          }
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void statusMonitorListsChestContentsAndJoinsNeighbours(GameTestHelper helper) {
    helper.setBlock(MACHINE, Blocks.CHEST.defaultBlockState());
    if (TestUtil.blockEntity(helper, MACHINE) instanceof ChestBlockEntity chest) {
      chest.setItem(0, new ItemStack(Items.COBBLESTONE, 40));
      chest.setItem(1, new ItemStack(Items.OAK_LOG, 3));
    }
    BlockEntityStatusMonitor monitor = panel(helper, MASTER);
    BlockPos second = MASTER.relative(BlockStatusMonitor.rightOf(Direction.NORTH), BlockStatusMonitor.WIDTH);
    panel(helper, second);
    probe(helper, monitor, MACHINE);
    helper.succeedWhen(() -> {
      if (monitor.status() != BlockEntityStatusMonitor.STATUS_OK) {
        throw TestUtil.assertion(helper, "monitor status " + monitor.status());
      }
      if (monitor.data().getIntOr(GenericStatusSources.TAG_ITEM_TOTAL, 0) != 43) {
        throw TestUtil.assertion(helper, "expected 43 items reported, got "
            + monitor.data().getIntOr(GenericStatusSources.TAG_ITEM_TOTAL, 0));
      }
      if (!monitor.joinedRight()) {
        throw TestUtil.assertion(helper, "the second panel was not detected as joined");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void statusMonitorCarriesJadeFluidDataForTank(GameTestHelper helper) {
    helper.setBlock(MACHINE, PipeRegistry.TANK.defaultBlockState());
    IFluidHandler handler = TestUtil.blockEntity(helper, MACHINE) != null
        ? CapabilityBridge.get(TestUtil.blockEntity(helper, MACHINE), ForgeCapabilities.FLUID_HANDLER, null) : null;
    if (handler == null) {
      helper.fail("tank has no fluid handler");
      return;
    }
    handler.fill(new FluidStack(ModFluids.OIL.still(), 4000), IFluidHandler.FluidAction.EXECUTE);
    BlockEntityStatusMonitor monitor = panel(helper, MASTER);
    probe(helper, monitor, MACHINE);
    boolean jade = ModList.get().isLoaded("jade");
    helper.succeedWhen(() -> {
      if (monitor.status() != BlockEntityStatusMonitor.STATUS_OK) {
        throw TestUtil.assertion(helper, "monitor status " + monitor.status());
      }
      if (monitor.data().getListOrEmpty(GenericStatusSources.TAG_FLUIDS).isEmpty()) {
        throw TestUtil.assertion(helper, "native fluid data missing");
      }
      BlockPos abs = helper.absolutePos(MACHINE);
      if (jade && (monitor.data().getIntOr("x", 0) != abs.getX() || !monitor.data().contains("id"))) {
        throw TestUtil.assertion(helper, "jade verification keys missing: " + monitor.data());
      }
      if (jade && !monitor.data().contains("JadeFluidStorage") && !monitor.data().contains("minecraft:fluid_storage")) {
        throw TestUtil.assertion(helper, "jade fluid storage data missing: " + monitor.data().keySet());
      }
    });
  }
}
