package com.faktocraft.gametest.machines;

import com.faktocraft.Faktocraft;
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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class StatusMonitorGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos MASTER = new BlockPos(2, 2, 6);
  private static final BlockPos MACHINE = new BlockPos(2, 1, 1);

  private static BlockEntityStatusMonitor panel(GameTestHelper helper, BlockPos master) {
    BlockStatusMonitor block = (BlockStatusMonitor) MonitorRegistry.STATUS_MONITOR;
    block.placePanel(helper.getLevel(), helper.absolutePos(master), Direction.NORTH);
    if (!(helper.getBlockEntity(master) instanceof BlockEntityStatusMonitor monitor)) {
      throw new GameTestAssertException("no monitor block entity at the master position");
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
      throw new GameTestAssertException("probe did not copy the block position");
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
    if (helper.getBlockEntity(MACHINE) instanceof FaktocraftBlockEntity machine) {
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
            throw new GameTestAssertException("monitor has no energy data yet, status " + monitor.status());
          }
          if (monitor.data().getInt(WailaData.TAG_ENERGY) <= 0) {
            throw new GameTestAssertException("stored energy was not reported");
          }
        })
        .thenExecute(() -> helper.setBlock(MACHINE, Blocks.AIR.defaultBlockState()))
        .thenWaitUntil(() -> {
          if (monitor.status() != BlockEntityStatusMonitor.STATUS_MISSING) {
            throw new GameTestAssertException("monitor did not notice the removed block, status "
                + monitor.status());
          }
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void statusMonitorListsChestContentsAndJoinsNeighbours(GameTestHelper helper) {
    helper.setBlock(MACHINE, Blocks.CHEST.defaultBlockState());
    if (helper.getBlockEntity(MACHINE) instanceof ChestBlockEntity chest) {
      chest.setItem(0, new ItemStack(Items.COBBLESTONE, 40));
      chest.setItem(1, new ItemStack(Items.OAK_LOG, 3));
    }
    BlockEntityStatusMonitor monitor = panel(helper, MASTER);
    BlockPos second = MASTER.relative(BlockStatusMonitor.rightOf(Direction.NORTH), BlockStatusMonitor.WIDTH);
    panel(helper, second);
    probe(helper, monitor, MACHINE);
    helper.succeedWhen(() -> {
      if (monitor.status() != BlockEntityStatusMonitor.STATUS_OK) {
        throw new GameTestAssertException("monitor status " + monitor.status());
      }
      if (monitor.data().getInt(GenericStatusSources.TAG_ITEM_TOTAL) != 43) {
        throw new GameTestAssertException("expected 43 items reported, got "
            + monitor.data().getInt(GenericStatusSources.TAG_ITEM_TOTAL));
      }
      if (!monitor.joinedRight()) {
        throw new GameTestAssertException("the second panel was not detected as joined");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void statusMonitorCarriesJadeFluidDataForTank(GameTestHelper helper) {
    helper.setBlock(MACHINE, PipeRegistry.TANK.defaultBlockState());
    IFluidHandler handler = helper.getBlockEntity(MACHINE) != null
        ? helper.getBlockEntity(MACHINE).getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null) : null;
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
        throw new GameTestAssertException("monitor status " + monitor.status());
      }
      if (monitor.data().getList(GenericStatusSources.TAG_FLUIDS, 10).isEmpty()) {
        throw new GameTestAssertException("native fluid data missing");
      }
      BlockPos abs = helper.absolutePos(MACHINE);
      if (jade && (monitor.data().getInt("x") != abs.getX() || !monitor.data().contains("id"))) {
        throw new GameTestAssertException("jade verification keys missing: " + monitor.data());
      }
      if (jade && !monitor.data().contains("JadeFluidStorage")) {
        throw new GameTestAssertException("jade fluid storage data missing: " + monitor.data().getAllKeys());
      }
    });
  }
}
