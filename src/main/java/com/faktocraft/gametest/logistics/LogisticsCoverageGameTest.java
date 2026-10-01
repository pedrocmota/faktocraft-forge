package com.faktocraft.gametest.logistics;

import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.CORE;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.PROVIDER;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.SETUP_DELAY;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.STOCK;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.TABLE;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.activeJobs;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.allSink;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.buildBus;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.core;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.countInChest;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.countInTable;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.fillEnergy;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.furnaceStation;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.historyCount;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.holder;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.ledgerState;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.machineAt;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.machineStation;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.pattern;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.pipeAt;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.placeChest;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.putModule;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.repeat;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.request;
import static com.faktocraft.gametest.logistics.LogisticsChainGameTest.simpleRecipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityAssemblyTable;
import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.ItemKey;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import com.faktocraft.common.block.impl.logistics.TaskLedger;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.function.Predicate;

public class LogisticsCoverageGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static BlockPos chassisAt(int x) {
    return new BlockPos(x, 1, 1);
  }

  private static BlockPos northChestOf(int x) {
    return new BlockPos(x, 1, 0);
  }

  private static ModuleSettings.FilterLine itemLine(Item item) {
    return new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM, new ItemStack(item), "", false, false, 0);
  }

  private static ModuleSettings.FilterLine textLine(ModuleSettings.LineMode mode, String text) {
    return new ModuleSettings.FilterLine(mode, ItemStack.EMPTY, text, false, false, 0);
  }

  private static ItemStack sinkWith(ModuleSettings.FilterLine line) {
    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setLine(sink, 0, line);
    return sink;
  }

  private static ItemStack overflowSink() {
    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setFlag(sink, ModuleSettings.FLAG_OVERFLOW, true);
    return sink;
  }

  private static void sideStation(GameTestHelper helper, int x, ItemStack module, ItemStack... chest) {
    placeChest(helper, northChestOf(x), chest);
    putModule(helper, chassisAt(x), 0, module);
  }

  private static int groundCount(GameTestHelper helper, Item item) {
    AABB box = new AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(0, 0, 0))),
        Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(12, 4, 12))));
    int count = 0;
    for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, box)) {
      if (entity.getItem().is(item)) {
        count += entity.getItem().getCount();
      }
    }
    return count;
  }

  private static int inHandler(com.faktocraft.common.util.ItemStackHandler handler, Predicate<ItemStack> what) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (what.test(stack)) {
        count += stack.getCount();
      }
    }
    return count;
  }

  private static void fillTable(GameTestHelper helper, ItemStack filler) {
    if (TestUtil.blockEntity(helper, TABLE) instanceof BlockEntityRequestTable table) {
      for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
        table.getItemStackHandler().setStackInSlot(i, filler.copy());
      }
    }
  }

  private static void expectHistory(GameTestHelper helper, String stateKey) {
    helper.succeedWhen(() -> {
      if (historyCount(helper, stateKey) != 1) {
        helper.fail("expected one '" + stateKey + "' record; " + ledgerState(helper));
        return;
      }
      if (activeJobs(helper) != 0) {
        helper.fail("no job should stay active; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void providerPriorityWinsOverDistance(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.COBBLESTONE, 8));
    ItemStack provider = new ItemStack(LogisticsRegistry.MODULE_PROVIDER);
    ModuleSettings.setPriority(provider, 5);
    sideStation(helper, 6, provider, new ItemStack(Items.COBBLESTONE, 8));
    request(helper, new ItemStack(Items.COBBLESTONE), 8);
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.COBBLESTONE)) != 8) {
        helper.fail("expected 8 cobblestone at the table; " + ledgerState(helper));
        return;
      }
      int far = countInChest(helper, northChestOf(6), stack -> stack.is(Items.COBBLESTONE));
      int near = countInChest(helper, STOCK, stack -> stack.is(Items.COBBLESTONE));
      if (far != 0 || near != 8) {
        helper.fail("expected the priority-5 provider to be drained first, found far=" + far + " near=" + near);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void providerMinReserveIsKept(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.COBBLESTONE, 8));
    if (TestUtil.blockEntity(helper, PROVIDER) instanceof BlockEntityChassis chassis) {
      ModuleSettings.setMinReserve(chassis.getModules().getStackInSlot(0), 4);
    }
    request(helper, new ItemStack(Items.COBBLESTONE), 8);
    request(helper, new ItemStack(Items.COBBLESTONE), 4);
    helper.succeedWhen(() -> {
      if (historyCount(helper, "error.missing") != 1 || historyCount(helper, "done") != 1) {
        helper.fail("expected the 8 to fail and the 4 to succeed; " + ledgerState(helper));
        return;
      }
      if (countInTable(helper, stack -> stack.is(Items.COBBLESTONE)) != 4
          || countInChest(helper, STOCK, stack -> stack.is(Items.COBBLESTONE)) != 4) {
        helper.fail("the reserve of 4 was not kept; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void providerExcludeFlagHidesItems(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.COBBLESTONE, 8), new ItemStack(Items.OAK_PLANKS, 8));
    if (TestUtil.blockEntity(helper, PROVIDER) instanceof BlockEntityChassis chassis) {
      ItemStack module = chassis.getModules().getStackInSlot(0);
      ModuleSettings.setLine(module, 0, itemLine(Items.COBBLESTONE));
      ModuleSettings.setFlag(module, ModuleSettings.FLAG_EXCLUDE, true);
    }
    request(helper, new ItemStack(Items.COBBLESTONE), 4);
    request(helper, new ItemStack(Items.OAK_PLANKS), 4);
    helper.succeedWhen(() -> {
      if (historyCount(helper, "error.missing") != 1 || historyCount(helper, "done") != 1) {
        helper.fail("expected cobblestone refused and planks delivered; " + ledgerState(helper));
        return;
      }
      if (countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) != 4
          || countInTable(helper, stack -> stack.is(Items.COBBLESTONE)) != 0) {
        helper.fail("wrong items at the table; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void sinkTagAndNamespaceLinesRoute(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    sideStation(helper, 5, sinkWith(textLine(ModuleSettings.LineMode.TAG, "minecraft:logs")));
    sideStation(helper, 6, sinkWith(textLine(ModuleSettings.LineMode.NAMESPACE, "faktocraft")));
    sideStation(helper, 8, overflowSink());
    sideStation(helper, 7, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR), new ItemStack(Items.OAK_LOG, 4),
        new ItemStack(ModItems.COPPER_DUST, 4), new ItemStack(Items.COBBLESTONE, 4));
    helper.succeedWhen(() -> {
      int logs = countInChest(helper, northChestOf(5), stack -> stack.is(Items.OAK_LOG));
      int dust = countInChest(helper, northChestOf(6), stack -> stack.is(ModItems.COPPER_DUST));
      int cobble = countInChest(helper, northChestOf(8), stack -> stack.is(Items.COBBLESTONE));
      int wrong = countInChest(helper, northChestOf(5), stack -> !stack.is(Items.OAK_LOG))
          + countInChest(helper, northChestOf(6), stack -> !stack.is(ModItems.COPPER_DUST))
          + countInChest(helper, northChestOf(8), stack -> !stack.is(Items.COBBLESTONE));
      if (logs != 4 || dust != 4 || cobble != 4 || wrong != 0) {
        helper.fail("routing wrong: logs=" + logs + " dust=" + dust + " cobble=" + cobble + " wrong=" + wrong);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void sinkNearestToTheCoreWinsTies(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    sideStation(helper, 3, allSink());
    sideStation(helper, 8, allSink());
    sideStation(helper, 7, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR), new ItemStack(Items.COBBLESTONE, 8));
    helper.succeedWhen(() -> {
      int near = countInChest(helper, northChestOf(3), stack -> stack.is(Items.COBBLESTONE));
      int far = countInChest(helper, northChestOf(8), stack -> stack.is(Items.COBBLESTONE));
      if (near != 8 || far != 0) {
        helper.fail("expected the sink closest to the core to win, found near=" + near + " far=" + far);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void fullSinkFallsBackToTheNextOne(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    sideStation(helper, 3, allSink(), repeat(Items.DIRT, 27));
    sideStation(helper, 8, allSink());
    sideStation(helper, 7, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR), new ItemStack(Items.COBBLESTONE, 8));
    helper.succeedWhen(() -> {
      int far = countInChest(helper, northChestOf(8), stack -> stack.is(Items.COBBLESTONE));
      if (far != 8) {
        helper.fail("expected the second sink to take the items, found " + far);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void extractorTagLineFiltersItems(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    sideStation(helper, 8, allSink());
    ItemStack extractor = new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR);
    ModuleSettings.setLine(extractor, 0, textLine(ModuleSettings.LineMode.TAG, "minecraft:logs"));
    sideStation(helper, 7, extractor, new ItemStack(Items.OAK_LOG, 4), new ItemStack(Items.COBBLESTONE, 4));
    helper.runAfterDelay(200, () -> {
      int logs = countInChest(helper, northChestOf(8), stack -> stack.is(Items.OAK_LOG));
      int cobbleMoved = countInChest(helper, northChestOf(8), stack -> stack.is(Items.COBBLESTONE));
      int cobbleLeft = countInChest(helper, northChestOf(7), stack -> stack.is(Items.COBBLESTONE));
      if (logs != 4 || cobbleMoved != 0 || cobbleLeft != 4) {
        helper.fail("filter ignored: logs=" + logs + " cobbleMoved=" + cobbleMoved + " cobbleLeft=" + cobbleLeft);
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void throughputUpgradeSpeedsUpTheExtractor(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    sideStation(helper, 8, allSink());
    sideStation(helper, 7, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR), new ItemStack(Items.COBBLESTONE, 64));
    sideStation(helper, 5, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR), new ItemStack(Items.OAK_LOG, 64));
    if (TestUtil.blockEntity(helper, chassisAt(7)) instanceof BlockEntityChassis chassis) {
      chassis.getUpgrades().setStackInSlot(0, new ItemStack(LogisticsRegistry.THROUGHPUT_UPGRADE));
    }
    helper.runAfterDelay(50, () -> {
      int upgraded = countInChest(helper, northChestOf(7), stack -> stack.is(Items.COBBLESTONE));
      int plain = countInChest(helper, northChestOf(5), stack -> stack.is(Items.OAK_LOG));
      if (upgraded != 0) {
        helper.fail("the upgraded extractor should have emptied its chest by now, " + upgraded + " left");
        return;
      }
      if (plain == 0) {
        helper.fail("the plain extractor emptied 64 items in 50 ticks, which is faster than 16 per op");
      }
    });
    helper.succeedWhen(() -> {
      if (countInChest(helper, northChestOf(5), stack -> stack.is(Items.OAK_LOG)) != 0) {
        helper.fail("plain extractor never finished");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void chassisMk2UsesItsThirdModuleSlot(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    helper.setBlock(chassisAt(7), LogisticsRegistry.CHASSIS_2.defaultBlockState());
    sideStation(helper, 8, allSink());
    placeChest(helper, northChestOf(7), new ItemStack(Items.COBBLESTONE, 8));
    putModule(helper, chassisAt(7), 2, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));
    helper.succeedWhen(() -> {
      if (countInChest(helper, northChestOf(8), stack -> stack.is(Items.COBBLESTONE)) != 8) {
        helper.fail("the extractor in slot 3 of a mk2 chassis did not run");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 900)
  public void supplierCraftsWhenAllowed(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 4));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    ItemStack supplier = new ItemStack(LogisticsRegistry.MODULE_SUPPLIER);
    ModuleSettings.setLine(supplier, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.OAK_PLANKS), "", false, false, 8));
    ModuleSettings.setFlag(supplier, ModuleSettings.FLAG_ALLOW_CRAFTS, true);
    sideStation(helper, 6, supplier);
    helper.succeedWhen(() -> {
      int planks = countInChest(helper, northChestOf(6), stack -> stack.is(Items.OAK_PLANKS));
      if (planks != 8) {
        helper.fail("expected the supplier to craft and stock 8 planks, found " + planks + "; "
            + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void supplierWithoutCraftsOnlyUsesStock(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 4));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    ItemStack supplier = new ItemStack(LogisticsRegistry.MODULE_SUPPLIER);
    ModuleSettings.setLine(supplier, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.OAK_PLANKS), "", false, false, 8));
    sideStation(helper, 6, supplier);
    helper.runAfterDelay(300, () -> {
      int planks = countInChest(helper, northChestOf(6), stack -> stack.is(Items.OAK_PLANKS));
      if (planks != 0) {
        helper.fail("the supplier crafted without permission: " + planks);
        return;
      }
      if (countInChest(helper, STOCK, stack -> stack.is(Items.OAK_LOG)) != 4) {
        helper.fail("logs were consumed without permission");
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void secondCoreOnTheNetworkRefusesRequests(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.COBBLESTONE, 8));
    helper.setBlock(new BlockPos(8, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    fillEnergy(helper, new BlockPos(8, 2, 1));
    request(helper, new ItemStack(Items.COBBLESTONE), 4);
    helper.runAfterDelay(SETUP_DELAY + 40, () -> {
      long conflicts = 0;
      for (BlockPos pos : List.of(CORE, new BlockPos(8, 2, 1))) {
        if (TestUtil.blockEntity(helper, pos) instanceof BlockEntityLogisticsController core) {
          conflicts += core.getLedger().userHistory().stream()
              .filter(record -> "error.conflict".equals(record.stateKey())).count();
        }
      }
      if (conflicts != 1) {
        helper.fail("expected one conflict failure, found " + conflicts + "; " + ledgerState(helper));
        return;
      }
      if (countInTable(helper, stack -> stack.is(Items.COBBLESTONE)) != 0) {
        helper.fail("items moved despite the conflict");
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void energyIsChargedPerMovedItem(GameTestHelper helper) {
    buildBus(helper, true, new boolean[] { false }, new ItemStack(Items.COBBLESTONE, 8));
    request(helper, new ItemStack(Items.COBBLESTONE), 8);
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.COBBLESTONE)) != 8) {
        helper.fail("expected 8 cobblestone at the table; " + ledgerState(helper));
        return;
      }
      BlockEntityLogisticsController core = core(helper);
      if (core == null) {
        helper.fail("no core");
        return;
      }
      int expected = core.getEnergyStorage().maxEnergy() - 16 * 2;
      if (core.getEnergyStorage().energyStored() != expected) {
        helper.fail("expected " + expected + " IE left after moving 16 items, found "
            + core.getEnergyStorage().energyStored());
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void requestQuantityIsCappedAtFourThousandNinetySix(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.COBBLESTONE, 8));
    request(helper, new ItemStack(Items.COBBLESTONE), 5000);
    helper.succeedWhen(() -> {
      BlockEntityLogisticsController core = core(helper);
      if (core == null || core.getLedger().userHistory().isEmpty()) {
        helper.fail("no history yet; " + ledgerState(helper));
        return;
      }
      TaskLedger.HistoryRecord record = core.getLedger().userHistory().get(0);
      if (!"error.missing".equals(record.stateKey()) || record.count() != 4096) {
        helper.fail("expected a missing failure for 4096 items, found " + record.stateKey() + " x"
            + record.count());
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void hugeCraftFailsAsTooComplex(GameTestHelper helper) {
    buildBus(helper, repeat(Items.OAK_LOG, 27));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    ItemStack planks = new ItemStack(Items.OAK_PLANKS);
    pattern(helper, 1, planks, planks, null, planks, planks);
    request(helper, new ItemStack(Items.CRAFTING_TABLE), 4096);
    expectHistory(helper, "error.too_complex");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void recipeCycleIsDetected(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.COBBLESTONE, 1));
    ItemStack nugget = new ItemStack(Items.IRON_NUGGET);
    pattern(helper, 0, nugget, nugget, nugget, nugget, nugget, nugget, nugget, nugget, nugget);
    pattern(helper, 1, new ItemStack(Items.IRON_INGOT));
    request(helper, new ItemStack(Items.IRON_INGOT), 1);
    expectHistory(helper, "error.cycle");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void historyKeepsOnlyTheLastHundred(GameTestHelper helper) {
    buildBus(helper, repeat(Items.COBBLESTONE, 2));
    request(helper, new ItemStack(Items.COBBLESTONE), 1, 101);
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.COBBLESTONE)) != 101) {
        helper.fail("expected 101 cobblestone at the table; " + ledgerState(helper));
        return;
      }
      BlockEntityLogisticsController core = core(helper);
      if (core == null || core.getLedger().userHistory().size() != TaskLedger.HISTORY_LIMIT) {
        helper.fail("history should be trimmed to " + TaskLedger.HISTORY_LIMIT);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void cancelFromAnotherOriginIsRefused(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 8));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    request(helper, new ItemStack(Items.OAK_PLANKS), 32);
    helper.runAfterDelay(SETUP_DELAY + 40, () -> {
      BlockEntityLogisticsController core = core(helper);
      if (core == null || core.getLedger().summaries().isEmpty()) {
        helper.fail("no job to cancel; " + ledgerState(helper));
        return;
      }
      long id = core.getLedger().summaries().get(0).id();
      if (core.getLedger().cancelUserJob(helper.getLevel(), id, helper.absolutePos(STOCK))) {
        helper.fail("a cancel from a different origin was accepted");
        return;
      }
      if (!core.getLedger().cancelUserJob(helper.getLevel(), id, helper.absolutePos(TABLE))) {
        helper.fail("a cancel from the right origin was refused");
      }
    });
    expectHistory(helper, "error.cancelled");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 900)
  public void twoTablesShareOneBench(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 8));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    BlockPos second = northChestOf(9);
    helper.setBlock(second, LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    request(helper, new ItemStack(Items.OAK_PLANKS), 4);
    helper.runAfterDelay(SETUP_DELAY, () -> {
      if (TestUtil.blockEntity(helper, second) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        table.request(null, 4);
      } else {
        helper.fail("no second table");
      }
    });
    helper.succeedWhen(() -> {
      int first = countInTable(helper, stack -> stack.is(Items.OAK_PLANKS));
      int other = TestUtil.blockEntity(helper, second) instanceof BlockEntityRequestTable table
          ? inHandler(table.getItemStackHandler(), stack -> stack.is(Items.OAK_PLANKS))
          : -1;
      if (first != 4 || other != 4) {
        helper.fail("expected 4 planks at each table, found " + first + " and " + other + "; "
            + ledgerState(helper));
        return;
      }
      if (historyCount(helper, "done") != 2) {
        helper.fail("expected two completed requests; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void sameBenchServesTwoStepsOfOneRequest(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 64));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    ItemStack planks = new ItemStack(Items.OAK_PLANKS);
    if (TestUtil.blockEntity(helper, pipeAt(0)) instanceof BlockEntityCraftPipe pipe) {
      int recipe = pipe.addRecipe();
      pipe.setPatternSlot(recipe, 0, planks);
      pipe.setPatternSlot(recipe, 3, planks);
    }
    request(helper, new ItemStack(Items.STICK), 16);
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.STICK)) != 16) {
        helper.fail("expected 16 sticks; " + ledgerState(helper));
        return;
      }
      if (historyCount(helper, "done") != 1 || activeJobs(helper) != 0) {
        helper.fail("unexpected ledger state; " + ledgerState(helper));
        return;
      }
      if (countInChest(helper, STOCK, stack -> stack.is(Items.OAK_LOG)) != 62) {
        helper.fail("expected 2 logs used");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void recipePipeWithTwoRecipesServesBoth(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.RAW_COPPER, 3), new ItemStack(Items.CLAY, 2));
    BlockEntityRecipePipe pipe = machineStation(helper, 0, M2Registry.CRUSHER);
    if (pipe == null) {
      return;
    }
    simpleRecipe(pipe, new ItemStack(Items.RAW_COPPER, 3), new ItemStack(ModItems.COPPER_DUST, 4));
    simpleRecipe(pipe, new ItemStack(Items.CLAY), new ItemStack(Items.CLAY_BALL, 2));
    request(helper, new ItemStack(ModItems.COPPER_DUST), 4);
    request(helper, new ItemStack(Items.CLAY_BALL), 4);
    helper.succeedWhen(() -> {
      int dust = countInTable(helper, stack -> stack.is(ModItems.COPPER_DUST));
      int balls = countInTable(helper, stack -> stack.is(Items.CLAY_BALL));
      if (dust != 4 || balls != 4) {
        helper.fail("expected 4 dust and 4 clay balls, found " + dust + "/" + balls + "; " + ledgerState(helper));
        return;
      }
      if (historyCount(helper, "done") != 2) {
        helper.fail("expected two completed requests; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void invalidBindingAndMissingMachineAreReported(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.RAW_COPPER, 3));
    BlockEntityRecipePipe bound = machineStation(helper, 0, M2Registry.CRUSHER);
    if (bound == null) {
      return;
    }
    int recipe = simpleRecipe(bound, new ItemStack(Items.RAW_COPPER, 3), new ItemStack(ModItems.COPPER_DUST, 4));
    bound.bind(recipe, 0, 0, "minecraft:chest", 27);
    helper.setBlock(pipeAt(1), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());
    if (TestUtil.blockEntity(helper, pipeAt(1)) instanceof BlockEntityRecipePipe lonely) {
      simpleRecipe(lonely, new ItemStack(Items.CLAY), new ItemStack(Items.CLAY_BALL, 2));
    }
    request(helper, new ItemStack(ModItems.COPPER_DUST), 4);
    helper.succeedWhen(() -> {
      BlockEntityLogisticsController core = core(helper);
      if (core == null || core.graph() == null) {
        helper.fail("no core");
        return;
      }
      core.collectDecls(core.graph());
      String errors = String.valueOf(core.getConfigErrors());
      if (!errors.contains("invalid_bind") || !errors.contains("no_machine")) {
        helper.fail("expected invalid_bind and no_machine config errors, got " + errors);
        return;
      }
      if (historyCount(helper, "error.missing") != 1) {
        helper.fail("the badly bound recipe should not be usable; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1800)
  public void furnaceKeepsPreexistingOutput(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.RAW_IRON, 8), new ItemStack(Items.COAL, 8));
    furnaceStation(helper, 0, new ItemStack(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT));
    if (TestUtil.blockEntity(helper, machineAt(0)) instanceof AbstractFurnaceBlockEntity furnace) {
      furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 5));
    }
    request(helper, new ItemStack(Items.IRON_INGOT), 4);
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.IRON_INGOT)) != 4) {
        helper.fail("expected exactly 4 ingots at the table; " + ledgerState(helper));
        return;
      }
      if (historyCount(helper, "done") != 1) {
        helper.fail("request not completed; " + ledgerState(helper));
        return;
      }
      if (!(TestUtil.blockEntity(helper, machineAt(0)) instanceof AbstractFurnaceBlockEntity furnace)
          || furnace.getItem(2).getCount() != 5) {
        helper.fail("the 5 ingots that were already in the furnace were touched");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void stalledMachineTimesOutAndReturnsInputs(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.RAW_IRON, 8));
    helper.setBlock(pipeAt(0), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());
    helper.setBlock(machineAt(0), Blocks.FURNACE.defaultBlockState());
    if (TestUtil.blockEntity(helper, pipeAt(0)) instanceof BlockEntityRecipePipe pipe) {
      simpleRecipe(pipe, new ItemStack(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT));
      pipe.setTimeoutTicks(200);
    }
    request(helper, new ItemStack(Items.IRON_INGOT), 4);
    helper.succeedWhen(() -> {
      if (historyCount(helper, "error.timeout") != 1 || activeJobs(helper) != 0) {
        helper.fail("expected a timeout failure; " + ledgerState(helper));
        return;
      }
      if (countInChest(helper, STOCK, stack -> stack.is(Items.RAW_IRON)) != 8) {
        helper.fail("raw iron was not returned to stock; " + ledgerState(helper));
        return;
      }
      if (!(TestUtil.blockEntity(helper, machineAt(0)) instanceof AbstractFurnaceBlockEntity furnace)
          || !furnace.getItem(0).isEmpty()) {
        helper.fail("raw iron left in the cold furnace");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 6000)
  public void singleSlotOutputGrowsBeyondSixtyFour(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.RAW_COPPER, 51));
    BlockEntityRecipePipe pipe = machineStation(helper, 0, M2Registry.CRUSHER);
    if (pipe == null) {
      return;
    }
    simpleRecipe(pipe, new ItemStack(Items.RAW_COPPER, 3), new ItemStack(ModItems.COPPER_DUST, 4));
    request(helper, new ItemStack(ModItems.COPPER_DUST), 68);
    helper.succeedWhen(() -> {
      int dust = countInTable(helper, stack -> stack.is(ModItems.COPPER_DUST));
      if (dust != 68) {
        helper.fail("expected 68 copper dust at the table, found " + dust + "; " + ledgerState(helper));
        return;
      }
      if (historyCount(helper, "done") != 1 || activeJobs(helper) != 0) {
        helper.fail("unexpected ledger state; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 2400)
  public void eightBenchesShareOneRequest(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 64));
    for (int station = 0; station < 8; station++) {
      pattern(helper, station, new ItemStack(Items.OAK_LOG));
    }
    int[] busiest = new int[1];
    helper.onEachTick(() -> {
      int busy = 0;
      for (int station = 0; station < 8; station++) {
        if (holder(helper, station) != 0) {
          busy++;
        }
      }
      busiest[0] = Math.max(busiest[0], busy);
    });
    request(helper, new ItemStack(Items.OAK_PLANKS), 256);
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) != 256) {
        helper.fail("expected 256 planks; " + ledgerState(helper));
        return;
      }
      if (historyCount(helper, "done") != 8) {
        helper.fail("expected the request split into 8 parts; " + ledgerState(helper));
        return;
      }
      if (busiest[0] < 8) {
        helper.fail("at most " + busiest[0] + " benches worked at once");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void recipePipeRemovedMidJobFails(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.RAW_IRON, 8), new ItemStack(Items.COAL, 8));
    furnaceStation(helper, 0, new ItemStack(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT));
    request(helper, new ItemStack(Items.IRON_INGOT), 4);
    helper.runAfterDelay(SETUP_DELAY + 120, () -> {
      if (holder(helper, 0) == 0) {
        helper.fail("the furnace was never claimed; " + ledgerState(helper));
        return;
      }
      helper.setBlock(pipeAt(0), Blocks.AIR.defaultBlockState());
    });
    expectHistory(helper, "error.lost");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void removingTheCoreDropsEverythingInCustody(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 64));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    request(helper, new ItemStack(Items.OAK_PLANKS), 64);
    helper.runAfterDelay(SETUP_DELAY + 200, () -> {
      if (countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) >= 64) {
        helper.fail("the job finished before the core was removed");
        return;
      }
      helper.setBlock(CORE, Blocks.AIR.defaultBlockState());
    });
    helper.runAfterDelay(SETUP_DELAY + 260, () -> {
      int logs = countInChest(helper, STOCK, stack -> stack.is(Items.OAK_LOG)) + groundCount(helper, Items.OAK_LOG);
      int planks = countInChest(helper, STOCK, stack -> stack.is(Items.OAK_PLANKS))
          + countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) + groundCount(helper, Items.OAK_PLANKS);
      if (TestUtil.blockEntity(helper, machineAt(0)) instanceof BlockEntityAssemblyTable table) {
        logs += inHandler(table.getIngredients(), stack -> stack.is(Items.OAK_LOG));
        planks += inHandler(table.getOutput(), stack -> stack.is(Items.OAK_PLANKS));
      }
      if (logs * 4 + planks != 256) {
        helper.fail("wood was lost when the core was removed: logs=" + logs + " planks=" + planks);
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void fullTableBouncesDeliveriesUntilThereIsRoom(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 8));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    fillTable(helper, new ItemStack(Items.DIRT, 64));
    request(helper, new ItemStack(Items.OAK_PLANKS), 4);
    helper.runAfterDelay(SETUP_DELAY + 300, () -> {
      if (historyCount(helper, "done") != 0 || activeJobs(helper) != 1) {
        helper.fail("the job should still be waiting for room; " + ledgerState(helper));
        return;
      }
      if (groundCount(helper, Items.OAK_PLANKS) != 0) {
        helper.fail("planks were dropped on the floor instead of kept in custody");
        return;
      }
      fillTable(helper, ItemStack.EMPTY);
    });
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) != 4 || historyCount(helper, "done") != 1) {
        helper.fail("planks never arrived after the table was emptied; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void unpoweredCorePausesAndResumes(GameTestHelper helper) {
    boolean[] powered = { true };
    buildBus(helper, true, powered, new ItemStack(Items.OAK_LOG, 8));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    request(helper, new ItemStack(Items.OAK_PLANKS), 16);
    int[] frozen = new int[1];
    helper.runAfterDelay(SETUP_DELAY + 60, () -> {
      powered[0] = false;
      BlockEntityLogisticsController core = core(helper);
      if (core != null) {
        core.getEnergyStorage().setEnergy(0);
      }
      frozen[0] = countInTable(helper, stack -> stack.is(Items.OAK_PLANKS));
    });
    helper.runAfterDelay(SETUP_DELAY + 360, () -> {
      int now = countInTable(helper, stack -> stack.is(Items.OAK_PLANKS));
      if (now != frozen[0] || now >= 16) {
        helper.fail("the network kept delivering without energy: " + frozen[0] + " -> " + now);
        return;
      }
      powered[0] = true;
    });
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) != 16 || historyCount(helper, "done") != 1) {
        helper.fail("the job did not resume after power came back; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1800)
  public void ledgerSurvivesSaveAndLoadMidJob(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 64));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    request(helper, new ItemStack(Items.OAK_PLANKS), 64);
    helper.runAfterDelay(SETUP_DELAY + 120, () -> {
      BlockEntityLogisticsController core = core(helper);
      if (core == null || activeJobs(helper) != 1) {
        helper.fail("no job in flight to save; " + ledgerState(helper));
        return;
      }
      CompoundTag saved = core.getLedger().save();
      core.getLedger().load(saved);
      if (activeJobs(helper) != 1) {
        helper.fail("the job did not survive the reload; " + ledgerState(helper));
      }
    });
    helper.succeedWhen(() -> {
      if (countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) != 64 || historyCount(helper, "done") != 1) {
        helper.fail("expected 64 planks after the reload; " + ledgerState(helper));
        return;
      }
      if (countInChest(helper, STOCK, stack -> stack.is(Items.OAK_LOG)) != 48) {
        helper.fail("expected exactly 16 logs used; " + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void collectorLinesFilterGroundItems(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    sideStation(helper, 8, allSink());
    ItemStack collector = new ItemStack(LogisticsRegistry.MODULE_COLLECTOR);
    ModuleSettings.setLine(collector, 0, itemLine(Items.COBBLESTONE));
    putModule(helper, chassisAt(6), 0, collector);
    helper.spawnItem(Items.COBBLESTONE, 6.5f, 2.5f, 1.5f);
    helper.spawnItem(Items.OAK_PLANKS, 6.5f, 2.5f, 1.5f);
    helper.runAfterDelay(200, () -> {
      int cobble = countInChest(helper, northChestOf(8), stack -> stack.is(Items.COBBLESTONE));
      int planks = countInChest(helper, northChestOf(8), stack -> stack.is(Items.OAK_PLANKS));
      if (cobble != 1 || planks != 0) {
        helper.fail("collector filter ignored: cobble=" + cobble + " planks=" + planks);
        return;
      }
      if (groundCount(helper, Items.OAK_PLANKS) != 1) {
        helper.fail("the planks should still be on the ground");
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void requestWithNbtVariantsInStock(GameTestHelper helper) {
    ItemStack damaged = new ItemStack(Items.IRON_PICKAXE);
    damaged.setDamageValue(10);
    buildBus(helper, new ItemStack(Items.IRON_PICKAXE), damaged);
    request(helper, new ItemStack(Items.IRON_PICKAXE), 1);
    helper.succeedWhen(() -> {
      int pristine = countInTable(helper, stack -> stack.is(Items.IRON_PICKAXE) && stack.getDamageValue() == 0);
      int worn = countInTable(helper, stack -> stack.is(Items.IRON_PICKAXE) && stack.getDamageValue() != 0);
      if (pristine != 1 || worn != 0) {
        helper.fail("expected only the undamaged pickaxe, found pristine=" + pristine + " worn=" + worn + "; "
            + ledgerState(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void chestFullOfDirtStillLetsPlanksThrough(GameTestHelper helper) {
    buildBus(helper, false, new boolean[] { true });
    sideStation(helper, 8, sinkWith(itemLine(Items.OAK_PLANKS)));
    sideStation(helper, 7, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR), new ItemStack(Items.OAK_PLANKS, 8),
        new ItemStack(Items.DIRT, 8));
    helper.runAfterDelay(200, () -> {
      int planks = countInChest(helper, northChestOf(8), stack -> stack.is(Items.OAK_PLANKS));
      int dirtLeft = countInChest(helper, northChestOf(7), stack -> stack.is(Items.DIRT));
      if (planks != 8 || dirtLeft != 8) {
        helper.fail("expected planks moved and dirt kept, found planks=" + planks + " dirtLeft=" + dirtLeft);
        return;
      }
      if (!(TestUtil.blockEntity(helper, northChestOf(7)) instanceof ChestBlockEntity)) {
        helper.fail("source chest vanished");
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void reservedStockIsNotOfferedTwice(GameTestHelper helper) {
    buildBus(helper, new ItemStack(Items.OAK_LOG, 2));
    pattern(helper, 0, new ItemStack(Items.OAK_LOG));
    request(helper, new ItemStack(Items.OAK_PLANKS), 8);
    request(helper, new ItemStack(Items.OAK_PLANKS), 8);
    helper.succeedWhen(() -> {
      long refused = historyCount(helper, "error.missing") + historyCount(helper, "error.stock_changed");
      if (historyCount(helper, "done") != 1 || refused != 1) {
        helper.fail("the second request should fail because the logs were reserved; " + ledgerState(helper));
        return;
      }
      if (countInTable(helper, stack -> stack.is(Items.OAK_PLANKS)) != 8) {
        helper.fail("expected 8 planks; " + ledgerState(helper));
        return;
      }
      BlockEntityLogisticsController core = core(helper);
      if (core != null && core.getLedger().reservedFor(ItemKey.of(Items.OAK_LOG)) != 0) {
        helper.fail("logs stayed reserved after completion");
      }
    });
  }
}
