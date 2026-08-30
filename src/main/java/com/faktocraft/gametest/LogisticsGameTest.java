package com.faktocraft.gametest;

import com.faktocraft.IndReb;
import com.faktocraft.common.block.impl.logistics.BlockEntityAssemblyTable;
import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(IndReb.MODID)
@PrefixGameTestTemplate(false)
public class LogisticsGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static com.faktocraft.common.block.impl.logistics.ItemKey key(
      net.minecraft.world.item.Item item) {
    return com.faktocraft.common.block.impl.logistics.ItemKey.of(item);
  }

  private static void placeChest(GameTestHelper helper, BlockPos rel, ItemStack... stacks) {
    helper.setBlock(rel, Blocks.CHEST.defaultBlockState());
    if (helper.getBlockEntity(rel) instanceof ChestBlockEntity chest) {
      for (int i = 0; i < stacks.length; i++) {
        chest.setItem(i, stacks[i]);
      }
    }
  }

  private static int countIn(GameTestHelper helper, BlockPos rel, net.minecraft.world.item.Item item) {
    if (!(helper.getBlockEntity(rel) instanceof ChestBlockEntity chest)) {
      return -1;
    }
    int count = 0;
    for (int i = 0; i < chest.getContainerSize(); i++) {
      ItemStack stack = chest.getItem(i);
      if (stack.is(item)) {
        count += stack.getCount();
      }
    }
    return count;
  }

  private static void fillCore(GameTestHelper helper, BlockPos rel) {
    if (helper.getBlockEntity(rel) instanceof IndRebBlockEntity be) {
      be.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.BASIC_CAPACITOR));
      be.getEnergyStorage().setEnergy(be.getEnergyStorage().maxEnergy());
    }
  }

  private static void putModule(GameTestHelper helper, BlockPos rel, int slot, ItemStack module) {
    if (helper.getBlockEntity(rel) instanceof BlockEntityChassis chassis) {
      chassis.getModules().setStackInSlot(slot, module);
    } else {
      helper.fail("no chassis at " + rel);
    }
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void chassisExtractorSortsBySink(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1),
        new ItemStack(Items.IRON_INGOT, 4), new ItemStack(Items.COBBLESTONE, 4));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(3, 1, 3));
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(4, 1, 3));

    fillCore(helper, new BlockPos(5, 1, 1));

    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));

    ItemStack ironSink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setLine(ironSink, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.IRON_INGOT), "", false, false, 0));
    putModule(helper, new BlockPos(3, 1, 2), 0, ironSink);

    ItemStack overflowSink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setFlag(overflowSink, ModuleSettings.FLAG_OVERFLOW, true);
    putModule(helper, new BlockPos(4, 1, 2), 0, overflowSink);

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(3, 1, 3), Items.IRON_INGOT) != 4) {
        helper.fail("iron not sorted into the filtered chest");
      }
      if (countIn(helper, new BlockPos(4, 1, 3), Items.COBBLESTONE) != 4) {
        helper.fail("cobblestone not sorted into the overflow chest");
      }
      if (countIn(helper, new BlockPos(1, 1, 1), Items.IRON_INGOT) != 0
          || countIn(helper, new BlockPos(1, 1, 1), Items.COBBLESTONE) != 0) {
        helper.fail("source chest was not emptied");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void partialStockSplitsTasks(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1),
        new ItemStack(Items.OAK_PLANKS, 8), new ItemStack(Items.OAK_LOG, 1));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());

    fillCore(helper, new BlockPos(3, 2, 1));

    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    fillCore(helper, new BlockPos(4, 1, 3));

    if (helper.getBlockEntity(new BlockPos(4, 1, 2)) instanceof BlockEntityCraftPipe pipe) {
      pipe.setPatternSlot(pipe.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    } else {
      helper.fail("no craft pipe");
    }

    helper.runAfterDelay(20, () -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table block entity");
        return;
      }
      table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
      table.request(null, 9);
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table block entity");
        return;
      }
      int planks = 0;
      for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
        ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
        if (stack.is(Items.OAK_PLANKS)) {
          planks += stack.getCount();
        }
      }
      if (planks != 9) {
        helper.fail("expected 9 planks in the table buffer, found " + planks);
      }
      if (!(helper.getBlockEntity(new BlockPos(3, 2, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core)) {
        helper.fail("no core");
        return;
      }
      List<com.faktocraft.common.block.impl.logistics.TaskLedger.HistoryRecord> done =
          core.getLedger().userHistory().stream()
              .filter(record -> "done".equals(record.stateKey())).toList();
      if (done.size() != 1) {
        helper.fail("expected 1 completed task in history, found " + done.size());
        return;
      }
      List<com.faktocraft.common.block.impl.logistics.TaskLedger.SubRecord> subs = done.get(0).subs();
      long deliver = subs.stream()
          .filter(sub -> "deliver".equals(sub.kind()) && sub.count() == 8 && "done".equals(sub.stateKey()))
          .count();
      long craft = subs.stream()
          .filter(sub -> "craft".equals(sub.kind()) && "done".equals(sub.stateKey())).count();
      if (deliver != 1 || craft != 1) {
        helper.fail("expected deliver(8)+craft subtasks, found deliver=" + deliver + " craft=" + craft);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void craftPipeAcceptsEquivalents(GameTestHelper helper) {
    buildEquivalenceSetup(helper, false);
    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.STICK));
        table.request(null, 4);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      int sticks = countInTable(helper, Items.STICK);
      if (sticks != 4) {
        helper.fail("expected 4 sticks crafted from birch planks, found " + sticks);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300)
  public void craftPipeStrictRefusesEquivalents(GameTestHelper helper) {
    buildEquivalenceSetup(helper, true);
    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.STICK));
        table.request(null, 4);
      } else {
        helper.fail("no request table");
      }
    });

    helper.runAfterDelay(200, () -> {
      int sticks = countInTable(helper, Items.STICK);
      if (sticks != 0) {
        helper.fail("an exact recipe must not accept birch planks, but crafted " + sticks + " sticks");
        return;
      }
      helper.succeed();
    });
  }

  private static void buildEquivalenceSetup(GameTestHelper helper, boolean strict) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.BIRCH_PLANKS, 16));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());

    fillCore(helper, new BlockPos(3, 2, 1));
    fillCore(helper, new BlockPos(4, 1, 3));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    if (helper.getBlockEntity(new BlockPos(4, 1, 2)) instanceof BlockEntityCraftPipe pipe) {
      int recipe = pipe.addRecipe();
      if (strict) {
        pipe.toggleStrict(recipe);
      }

      pipe.setPatternSlot(recipe, 0, new ItemStack(Items.OAK_PLANKS));
      pipe.setPatternSlot(recipe, 3, new ItemStack(Items.OAK_PLANKS));
    } else {
      helper.fail("no craft pipe");
    }
  }

  private static int countInTable(GameTestHelper helper, net.minecraft.world.item.Item item) {
    if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
      helper.fail("no request table");
      return 0;
    }
    int found = 0;
    for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
      ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
      if (stack.is(item)) {
        found += stack.getCount();
      }
    }
    return found;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void craftPipeMachineMode(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 1));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());
    placeChest(helper, new BlockPos(4, 1, 3));

    fillCore(helper, new BlockPos(3, 2, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    if (helper.getBlockEntity(new BlockPos(4, 1, 2))
        instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe pipe) {
      int recipe = pipe.addRecipe();
      pipe.setIo(recipe, 0, new ItemStack(Items.OAK_LOG));
      pipe.setIo(recipe, com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe.outputId(0),
          new ItemStack(Items.OAK_PLANKS));
    } else {
      helper.fail("no recipe pipe");
      return;
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        table.request(null, 1);
      } else {
        helper.fail("no request table");
      }
    });

    helper.runAfterDelay(120, () -> {
      if (helper.getBlockEntity(new BlockPos(4, 1, 3)) instanceof ChestBlockEntity machine) {
        for (int i = 0; i < machine.getContainerSize(); i++) {
          if (machine.getItem(i).is(Items.OAK_LOG)) {
            machine.setItem(i, new ItemStack(Items.OAK_PLANKS, 1));
            return;
          }
        }
        helper.fail("the craft pipe never fed the machine");
      }
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table");
        return;
      }
      int planks = 0;
      for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
        ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
        if (stack.is(Items.OAK_PLANKS)) {
          planks += stack.getCount();
        }
      }
      if (planks != 1) {
        helper.fail("expected the machine result delivered to the table, found " + planks);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public void craftPipeSlotBinding(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 1));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());
    placeChest(helper, new BlockPos(4, 1, 3));

    fillCore(helper, new BlockPos(3, 2, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    if (helper.getBlockEntity(new BlockPos(4, 1, 2))
        instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe pipe) {
      int recipe = pipe.addRecipe();
      pipe.setIo(recipe, 0, new ItemStack(Items.OAK_LOG));
      pipe.setIo(recipe, com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe.outputId(0),
          new ItemStack(Items.OAK_PLANKS));
      pipe.bind(recipe, 0, 5, "minecraft:chest", 27);
    } else {
      helper.fail("no recipe pipe");
      return;
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        table.request(null, 1);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(4, 1, 3)) instanceof ChestBlockEntity machine)) {
        helper.fail("no machine chest");
        return;
      }
      if (!machine.getItem(5).is(Items.OAK_LOG)) {
        helper.fail("the bound slot never received the ingredient");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 900)
  public void twoRequestsShareOneStation(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 2));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());

    fillCore(helper, new BlockPos(3, 2, 1));
    fillCore(helper, new BlockPos(4, 1, 3));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    if (helper.getBlockEntity(new BlockPos(4, 1, 2)) instanceof BlockEntityCraftPipe pipe) {
      pipe.setPatternSlot(pipe.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    } else {
      helper.fail("no craft pipe");
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        table.request(null, 4);
        table.request(null, 4);
      } else {
        helper.fail("no request table");
      }
    });

    helper.runAfterDelay(40, () -> {
      if (helper.getBlockEntity(new BlockPos(3, 2, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core) {
        if (core.getLedger().stationHolder(helper.absolutePos(new BlockPos(4, 1, 3))) == 0) {
          helper.fail("the assembly table was never claimed by a job");
        }
      }
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table");
        return;
      }
      int planks = 0;
      for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
        ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
        if (stack.is(Items.OAK_PLANKS)) {
          planks += stack.getCount();
        }
      }
      if (planks != 8) {
        helper.fail("expected both requests delivered (8 planks), found " + planks);
      }
      if (!(helper.getBlockEntity(new BlockPos(3, 2, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core)) {
        helper.fail("no core");
        return;
      }
      List<com.faktocraft.common.block.impl.logistics.TaskLedger.HistoryRecord> history =
          core.getLedger().userHistory();
      long done = history.stream().filter(record -> "done".equals(record.stateKey())).count();
      if (done != 2 || history.size() != 2) {
        helper.fail("expected 2 completed tasks and no failure, found " + done + " of " + history.size());
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 900)
  public void twoStationsRunInParallel(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 2));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());

    fillCore(helper, new BlockPos(3, 2, 1));
    fillCore(helper, new BlockPos(4, 1, 3));
    fillCore(helper, new BlockPos(2, 1, 3));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    for (BlockPos pipePos : List.of(new BlockPos(4, 1, 2), new BlockPos(2, 1, 2))) {
      if (helper.getBlockEntity(pipePos) instanceof BlockEntityCraftPipe pipe) {
        pipe.setPatternSlot(pipe.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
      } else {
        helper.fail("no craft pipe at " + pipePos);
      }
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        table.request(null, 4);
        table.request(null, 4);
      } else {
        helper.fail("no request table");
      }
    });

    helper.runAfterDelay(45, () -> {
      if (!(helper.getBlockEntity(new BlockPos(3, 2, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core)) {
        helper.fail("no core");
        return;
      }
      long first = core.getLedger().stationHolder(helper.absolutePos(new BlockPos(4, 1, 3)));
      long second = core.getLedger().stationHolder(helper.absolutePos(new BlockPos(2, 1, 3)));
      if (first == 0 || second == 0) {
        helper.fail("both tables should be claimed, got " + first + " and " + second);
        return;
      }
      if (first == second) {
        helper.fail("the same job took both tables");
      }
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table");
        return;
      }
      int planks = 0;
      for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
        ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
        if (stack.is(Items.OAK_PLANKS)) {
          planks += stack.getCount();
        }
      }
      if (planks != 8) {
        helper.fail("expected both requests delivered (8 planks), found " + planks);
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void sinkPriorityChangeRedirects(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.IRON_INGOT, 4));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(3, 1, 3));
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(4, 1, 3));

    fillCore(helper, new BlockPos(5, 1, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));

    ItemStack sinkA = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setLine(sinkA, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.IRON_INGOT), "", false, false, 0));
    ModuleSettings.setPriority(sinkA, 1);
    putModule(helper, new BlockPos(3, 1, 2), 0, sinkA);

    ItemStack sinkB = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setLine(sinkB, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.IRON_INGOT), "", false, false, 0));
    putModule(helper, new BlockPos(4, 1, 2), 0, sinkB);

    helper.runAfterDelay(120, () -> {
      if (countIn(helper, new BlockPos(3, 1, 3), Items.IRON_INGOT) != 4) {
        helper.fail("first batch should have landed in the priority-1 sink");
        return;
      }
      if (helper.getBlockEntity(new BlockPos(4, 1, 2))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityChassis chassis) {
        ModuleSettings.setPriority(chassis.getModules().getStackInSlot(0), 5);
      } else {
        helper.fail("no sink chassis");
        return;
      }
      if (helper.getBlockEntity(new BlockPos(1, 1, 1))
          instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
        chest.setItem(0, new ItemStack(Items.IRON_INGOT, 4));
      } else {
        helper.fail("no source chest");
      }
    });

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(4, 1, 3), Items.IRON_INGOT) != 4) {
        helper.fail("second batch should follow the raised priority");
      }
      if (countIn(helper, new BlockPos(3, 1, 3), Items.IRON_INGOT) != 4) {
        helper.fail("first batch should stay where it was");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void providerAddedAtRuntimeIsConsulted(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_PLANKS, 8));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());

    fillCore(helper, new BlockPos(3, 2, 1));

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        table.request(null, 8);
      } else {
        helper.fail("no request table");
      }
    });
    helper.runAfterDelay(60, () -> {
      if (countInTable(helper, Items.OAK_PLANKS) != 0) {
        helper.fail("request without providers should deliver nothing");
        return;
      }
      putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.request(null, 8);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      if (countInTable(helper, Items.OAK_PLANKS) != 8) {
        helper.fail("the runtime provider was not consulted");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void cancelRequestReturnsItems(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 1));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());

    helper.setBlock(new BlockPos(4, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(2, 1, 3));

    fillCore(helper, new BlockPos(3, 2, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    ItemStack logSink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setLine(logSink, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.OAK_LOG), "", false, false, 0));
    putModule(helper, new BlockPos(2, 1, 2), 0, logSink);

    if (helper.getBlockEntity(new BlockPos(4, 1, 2)) instanceof BlockEntityCraftPipe pipe) {
      pipe.setPatternSlot(pipe.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    } else {
      helper.fail("no craft pipe");
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        table.request(null, 4);
      } else {
        helper.fail("no request table");
      }
    });

    helper.runAfterDelay(120, () -> {
      if (!(helper.getBlockEntity(new BlockPos(3, 2, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core)) {
        helper.fail("no core");
        return;
      }
      if (!(helper.getBlockEntity(new BlockPos(4, 1, 3))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityAssemblyTable table)
          || countInHandler(table.getIngredients(), Items.OAK_LOG) != 1) {
        helper.fail("the log should be waiting in the assembly ingredients");
        return;
      }
      long jobId = 0;
      for (com.faktocraft.common.block.impl.logistics.TaskLedger.TaskSummary summary
          : core.getLedger().summaries()) {
        if (!summary.system() && summary.id() != 0) {
          jobId = summary.id();
          break;
        }
      }
      if (jobId == 0) {
        helper.fail("no active user job to cancel");
        return;
      }
      if (!core.getLedger().cancelUserJob(helper.getLevel(), jobId, null)) {
        helper.fail("cancel was refused");
      }
    });

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(2, 1, 3), Items.OAK_LOG) != 1) {
        helper.fail("the pulled-back log should land in the sink chest");
        return;
      }
      if (!(helper.getBlockEntity(new BlockPos(3, 2, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core)) {
        helper.fail("no core");
        return;
      }
      if (core.getLedger().activeJobCount() != 0) {
        helper.fail("the cancelled job should be gone");
        return;
      }
      var cancelledRecord = core.getLedger().userHistory().stream()
          .filter(record -> "error.cancelled".equals(record.stateKey())).findFirst().orElse(null);
      if (cancelledRecord == null) {
        helper.fail("history should show the cancellation");
        return;
      }
      boolean frozen = cancelledRecord.subs().stream().anyMatch(sub -> !"done".equals(sub.stateKey())
          && !"cancelled".equals(sub.stateKey()));
      if (frozen) {
        helper.fail("unfinished subtasks should be marked cancelled too");
      }
    });
  }

  private static int countInHandler(com.faktocraft.common.util.ItemStackHandler handler,
      net.minecraft.world.item.Item item) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (stack.is(item)) {
        count += stack.getCount();
      }
    }
    return count;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void treeSinkRoutesByOverrides(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.IRON_INGOT, 4));
    if (helper.getBlockEntity(new BlockPos(1, 1, 1))
        instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
      chest.setItem(1, new ItemStack(Items.COBBLESTONE, 4));
    }
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(3, 1, 3));
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(4, 1, 3));

    fillCore(helper, new BlockPos(5, 1, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));

    ItemStack ironSink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    java.util.Map<String, Boolean> allowIron = new java.util.LinkedHashMap<>();
    allowIron.put(com.faktocraft.common.block.impl.logistics.LogisticsItemTree.itemNode(Items.IRON_INGOT), true);
    ModuleSettings.putTreeOverrides(ironSink, allowIron);
    putModule(helper, new BlockPos(3, 1, 2), 0, ironSink);

    ItemStack overflowSink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.putTreeOverrides(overflowSink, new java.util.LinkedHashMap<>());
    ModuleSettings.setFlag(overflowSink, ModuleSettings.FLAG_OVERFLOW, true);
    putModule(helper, new BlockPos(4, 1, 2), 0, overflowSink);

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(3, 1, 3), Items.IRON_INGOT) != 4) {
        helper.fail("iron should land in the sink that allows it");
      }
      if (countIn(helper, new BlockPos(4, 1, 3), Items.COBBLESTONE) != 4) {
        helper.fail("cobblestone should fall back to the overflow sink");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void treeCategoryOverrideResolves(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.IRON_INGOT, 4));
    if (helper.getBlockEntity(new BlockPos(1, 1, 1))
        instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
      chest.setItem(1, new ItemStack(Items.COBBLESTONE, 4));
    }
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(3, 1, 3));

    fillCore(helper, new BlockPos(4, 1, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));

    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    java.util.Map<String, Boolean> overrides = new java.util.LinkedHashMap<>();
    overrides.put(com.faktocraft.common.block.impl.logistics.LogisticsItemTree.NODE_ALL, true);
    overrides.put(com.faktocraft.common.block.impl.logistics.LogisticsItemTree.namespaceNode("minecraft"), false);
    overrides.put(com.faktocraft.common.block.impl.logistics.LogisticsItemTree.categoryNode("minecraft",
        new net.minecraft.resources.ResourceLocation("minecraft", "ingredients")), true);
    ModuleSettings.putTreeOverrides(sink, overrides);
    putModule(helper, new BlockPos(3, 1, 2), 0, sink);

    helper.runAfterDelay(120, () -> {
      if (countIn(helper, new BlockPos(1, 1, 1), Items.COBBLESTONE) != 4) {
        helper.fail("cobblestone is denied by the namespace override and should stay put");
      }
    });
    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(3, 1, 3), Items.IRON_INGOT) != 4) {
        helper.fail("the category override should let iron through");
      }
      if (countIn(helper, new BlockPos(1, 1, 1), Items.COBBLESTONE) != 4) {
        helper.fail("cobblestone should never move");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public void legacyEmptyTreeProviderStillStocks(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.IRON_INGOT, 62));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    fillCore(helper, new BlockPos(3, 1, 1));

    ItemStack provider = new ItemStack(LogisticsRegistry.MODULE_PROVIDER);
    ModuleSettings.putTreeOverrides(provider, new java.util.LinkedHashMap<>());
    provider.getOrCreateTag().getCompound("logistics").remove("treeV");
    putModule(helper, new BlockPos(2, 1, 1), 0, provider);

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(3, 1, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core)) {
        helper.fail("no core");
        return;
      }
      com.faktocraft.common.block.impl.logistics.LogisticsGraph graph = core.graph();
      if (graph == null) {
        helper.fail("no graph yet");
        return;
      }
      java.util.Map<com.faktocraft.common.block.impl.logistics.ItemKey, Integer> stock =
          com.faktocraft.common.block.impl.logistics.BlockEntityChassis.stockSnapshot(
              helper.getLevel(), graph, core.getLedger());
      if (stock.getOrDefault(key(Items.IRON_INGOT), 0) != 62) {
        helper.fail("the legacy provider should still offer its iron, saw "
            + stock.getOrDefault(key(Items.IRON_INGOT), 0));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void treeExtractorPullsOnlyMarkedItems(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.IRON_INGOT, 4));
    if (helper.getBlockEntity(new BlockPos(1, 1, 1))
        instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
      chest.setItem(1, new ItemStack(Items.COBBLESTONE, 4));
    }
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(3, 1, 3));
    fillCore(helper, new BlockPos(4, 1, 1));

    ItemStack extractor = new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR);
    java.util.Map<String, Boolean> onlyIron = new java.util.LinkedHashMap<>();
    onlyIron.put(com.faktocraft.common.block.impl.logistics.LogisticsItemTree.itemNode(Items.IRON_INGOT), true);
    ModuleSettings.putTreeOverrides(extractor, onlyIron);
    putModule(helper, new BlockPos(2, 1, 1), 0, extractor);

    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    java.util.Map<String, Boolean> everything = new java.util.LinkedHashMap<>();
    everything.put(com.faktocraft.common.block.impl.logistics.LogisticsItemTree.NODE_ALL, true);
    ModuleSettings.putTreeOverrides(sink, everything);
    putModule(helper, new BlockPos(3, 1, 2), 0, sink);

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(3, 1, 3), Items.IRON_INGOT) != 4) {
        helper.fail("the marked iron should have been pulled");
      }
      if (countIn(helper, new BlockPos(1, 1, 1), Items.COBBLESTONE) != 4) {
        helper.fail("unmarked cobblestone must stay in the source chest");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void treeSupplierKeepsTargetStock(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.IRON_INGOT, 32));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(3, 1, 3));
    fillCore(helper, new BlockPos(4, 1, 1));

    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    ItemStack supplier = new ItemStack(LogisticsRegistry.MODULE_SUPPLIER);
    String node = com.faktocraft.common.block.impl.logistics.LogisticsItemTree.itemNode(Items.IRON_INGOT);
    java.util.Map<String, Boolean> marked = new java.util.LinkedHashMap<>();
    marked.put(node, true);
    ModuleSettings.putTreeOverrides(supplier, marked);
    java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
    counts.put(node, 8);
    ModuleSettings.putTreeCounts(supplier, counts);
    putModule(helper, new BlockPos(3, 1, 2), 0, supplier);

    helper.succeedWhen(() -> {
      int held = countIn(helper, new BlockPos(3, 1, 3), Items.IRON_INGOT);
      if (held != 8) {
        helper.fail("the supplier should hold exactly its target of 8, saw " + held);
      }
    });
  }

  private static ItemStack cell(int milliBuckets) {
    ItemStack stack = new ItemStack(ModItems.FLUID_CELL);
    if (milliBuckets > 0) {
      com.faktocraft.common.item.base.FluidItem.setFluid(stack,
          net.minecraft.world.level.material.Fluids.WATER, milliBuckets);
    }
    return stack;
  }

  private static int fluidInTable(GameTestHelper helper, BlockPos rel) {
    if (!(helper.getBlockEntity(rel) instanceof BlockEntityRequestTable table)) {
      helper.fail("no request table");
      return -1;
    }
    for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
      ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
      if (stack.is(ModItems.FLUID_CELL)) {
        return com.faktocraft.common.item.base.FluidItem.getFluidAmount(stack);
      }
    }
    return -1;
  }

  private static void buildRequestNetwork(GameTestHelper helper, ItemStack... provided) {
    placeChest(helper, new BlockPos(1, 1, 1), provided);
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    fillCore(helper, new BlockPos(3, 2, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void tableAutoExtractDrainsDeposit(GameTestHelper helper) {
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(1, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(2, 1, 3));
    fillCore(helper, new BlockPos(3, 1, 1));

    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    java.util.Map<String, Boolean> everything = new java.util.LinkedHashMap<>();
    everything.put(com.faktocraft.common.block.impl.logistics.LogisticsItemTree.NODE_ALL, true);
    ModuleSettings.putTreeOverrides(sink, everything);
    putModule(helper, new BlockPos(2, 1, 2), 0, sink);

    if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof BlockEntityRequestTable table)) {
      helper.fail("no request table");
      return;
    }
    table.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 12));

    helper.runAfterDelay(60, () -> {
      if (helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof BlockEntityRequestTable current) {
        if (countInHandler(current.getItemStackHandler(), Items.IRON_INGOT) != 12) {
          helper.fail("the deposit must not drain while auto-extract is off");
          return;
        }
        current.setAutoExtract(true);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(2, 1, 3), Items.IRON_INGOT) != 12) {
        helper.fail("the deposit should have been drained into the sink");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public void blockedCraftReportsRootCause(GameTestHelper helper) {
    com.faktocraft.common.block.impl.logistics.Endpoint end =
        com.faktocraft.common.block.impl.logistics.Endpoint.inventory(BlockPos.ZERO);
    com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl planks =
        new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl(BlockPos.ZERO, 0,
            key(Items.OAK_PLANKS), 4,
            List.of(com.faktocraft.common.block.impl.logistics.LogisticsPlanner.ItemChoice.of(
                key(Items.OAK_LOG), 1)),
            List.of(end), end, 0, false, 0);
    com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl door =
        new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl(BlockPos.ZERO, 1,
            key(Items.OAK_DOOR), 3,
            List.of(com.faktocraft.common.block.impl.logistics.LogisticsPlanner.ItemChoice.of(
                key(Items.OAK_PLANKS), 6)),
            List.of(end), end, 0, false, 0);
    var index = com.faktocraft.common.block.impl.logistics.LogisticsPlanner.index(List.of(planks, door));

    com.faktocraft.common.block.impl.logistics.ItemKey missing =
        com.faktocraft.common.block.impl.logistics.LogisticsPlanner
            .blockingIngredient(key(Items.OAK_DOOR), index, java.util.Set.of());
    if (!missing.equals(key(Items.OAK_LOG))) {
      helper.fail("expected oak log as the root cause, got " + missing);
    }

    java.util.Set<com.faktocraft.common.block.impl.logistics.ItemKey> known =
        com.faktocraft.common.block.impl.logistics.LogisticsPlanner.craftableSet(
            java.util.Map.of(key(Items.OAK_LOG), 4), List.of(planks, door));
    if (!known.contains(key(Items.OAK_DOOR))) {
      helper.fail("door should be craftable once logs are in stock");
    }

    if (!com.faktocraft.common.block.impl.logistics.LogisticsPlanner
        .producibleSet(java.util.Map.of(key(Items.OAK_DOOR), 34), List.of()).isEmpty()) {
      helper.fail("stocked items must not be reported as producible without a recipe");
    }
    java.util.Set<com.faktocraft.common.block.impl.logistics.ItemKey> producible =
        com.faktocraft.common.block.impl.logistics.LogisticsPlanner.producibleSet(
            java.util.Map.of(key(Items.OAK_LOG), 4), List.of(planks, door));
    if (producible.contains(key(Items.OAK_LOG))) {
      helper.fail("logs have no recipe, so they are not producible");
    }
    if (!producible.contains(key(Items.OAK_PLANKS)) || !producible.contains(key(Items.OAK_DOOR))) {
      helper.fail("planks and door should be producible from logs");
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public void flexibleIngredientBacktracksToCraftableVariant(GameTestHelper helper) {
    com.faktocraft.common.block.impl.logistics.Endpoint end =
        com.faktocraft.common.block.impl.logistics.Endpoint.inventory(BlockPos.ZERO);
    com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl junglePlanks =
        new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl(BlockPos.ZERO, 0,
            key(Items.JUNGLE_PLANKS), 4,
            List.of(com.faktocraft.common.block.impl.logistics.LogisticsPlanner.ItemChoice.of(
                key(Items.JUNGLE_LOG), 1)),
            List.of(end), end, 0, false, 0);
    com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl oakPlanks =
        new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl(BlockPos.ZERO, 1,
            key(Items.OAK_PLANKS), 4,
            List.of(com.faktocraft.common.block.impl.logistics.LogisticsPlanner.ItemChoice.of(
                key(Items.OAK_LOG), 1)),
            List.of(end), end, 0, false, 0);

    com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl sticks =
        new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.CraftDecl(BlockPos.ZERO, 2,
            key(Items.STICK), 4,
            List.of(new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.ItemChoice(
                List.of(key(Items.JUNGLE_PLANKS), key(Items.OAK_PLANKS)), 2)),
            List.of(end), end, 0, false, 0);

    com.faktocraft.common.block.impl.logistics.LogisticsPlanner.Plan plan =
        com.faktocraft.common.block.impl.logistics.LogisticsPlanner.plan(
            new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.PlanRequest(
                key(Items.STICK), 4, java.util.Map.of(key(Items.OAK_LOG), 56),
                List.of(junglePlanks, oakPlanks, sticks)));
    if (!plan.success()) {
      helper.fail("the plan should fall back to oak planks, but failed missing "
          + plan.missingItem());
      return;
    }
    boolean usesOak = plan.steps().stream().anyMatch(step ->
        step.decl().result().equals(key(Items.STICK)) && step.ingredients().stream().anyMatch(
            ingredient -> ingredient.item().equals(key(Items.OAK_PLANKS))));
    if (!usesOak) {
      helper.fail("the stick step should consume crafted oak planks");
      return;
    }

    com.faktocraft.common.block.impl.logistics.LogisticsPlanner.Plan blocked =
        com.faktocraft.common.block.impl.logistics.LogisticsPlanner.plan(
            new com.faktocraft.common.block.impl.logistics.LogisticsPlanner.PlanRequest(
                key(Items.STICK), 4, java.util.Map.of(),
                List.of(junglePlanks, oakPlanks, sticks)));
    if (blocked.success() || !key(Items.JUNGLE_LOG).equals(blocked.missingItem())) {
      helper.fail("expected a failure missing jungle log, got "
          + (blocked.success() ? "success" : String.valueOf(blocked.missingItem())));
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void supplierCreatesSystemTask(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_PLANKS, 8));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(2, 1, 3));

    fillCore(helper, new BlockPos(3, 2, 1));

    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));
    ItemStack supplier = new ItemStack(LogisticsRegistry.MODULE_SUPPLIER);
    ModuleSettings.setLine(supplier, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.OAK_PLANKS), "", false, false, 4));
    putModule(helper, new BlockPos(2, 1, 2), 0, supplier);

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(2, 1, 3), Items.OAK_PLANKS) != 4) {
        helper.fail("supplier did not stock 4 planks");
      }
      if (!(helper.getBlockEntity(new BlockPos(3, 2, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core)) {
        helper.fail("no core");
        return;
      }
      long systemDone = core.getLedger().systemHistory().stream()
          .filter(record -> "done".equals(record.stateKey()) && record.system()).count();
      if (systemDone != 1) {
        helper.fail("expected 1 completed system task, found " + systemDone);
      }
      if (!core.getLedger().userHistory().isEmpty()) {
        helper.fail("supplier task leaked into the user history");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void assemblyLeftoverDrainsToSink(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 1));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 2), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    placeChest(helper, new BlockPos(2, 1, 3));

    fillCore(helper, new BlockPos(3, 2, 1));

    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    fillCore(helper, new BlockPos(4, 1, 3));
    ItemStack overflowSink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setFlag(overflowSink, ModuleSettings.FLAG_OVERFLOW, true);
    putModule(helper, new BlockPos(2, 1, 2), 0, overflowSink);

    if (helper.getBlockEntity(new BlockPos(4, 1, 2)) instanceof BlockEntityCraftPipe pipe) {
      pipe.setPatternSlot(pipe.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    } else {
      helper.fail("no craft pipe");
    }

    helper.runAfterDelay(20, () -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table block entity");
        return;
      }
      table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
      table.request(null, 1);
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table block entity");
        return;
      }
      int planks = 0;
      for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
        ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
        if (stack.is(Items.OAK_PLANKS)) {
          planks += stack.getCount();
        }
      }
      if (planks != 1) {
        helper.fail("expected 1 plank in the table buffer, found " + planks);
      }
      if (countIn(helper, new BlockPos(2, 1, 3), Items.OAK_PLANKS) != 3) {
        int inOutput = 0;
        if (helper.getBlockEntity(new BlockPos(4, 1, 3)) instanceof BlockEntityAssemblyTable assembly) {
          for (int i = 0; i < assembly.getOutput().getSlots(); i++) {
            ItemStack stack = assembly.getOutput().getStackInSlot(i);
            if (stack.is(Items.OAK_PLANKS)) {
              inOutput += stack.getCount();
            }
          }
        }
        String diag = "";
        if (helper.getBlockEntity(new BlockPos(3, 2, 1))
            instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core) {
          diag = " ledger=" + core.getLedger().summaries() + " energy=" + core.getEnergyStorage().energyStored();
        }
        int ground = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            new net.minecraft.world.phys.AABB(helper.absolutePos(BlockPos.ZERO),
                helper.absolutePos(new BlockPos(7, 5, 7)))).size();
        helper.fail("leftover planks did not drain to the sink chest (sink="
            + countIn(helper, new BlockPos(2, 1, 3), Items.OAK_PLANKS) + " output=" + inOutput
            + " ground=" + ground + diag + ")");
      }
      if (helper.getBlockEntity(new BlockPos(4, 1, 3)) instanceof BlockEntityAssemblyTable assembly
          && !assembly.getOutput().isEmpty()) {
        helper.fail("assembly output was not drained");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void requestTableBenchCraft(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 2));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 3), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());

    fillCore(helper, new BlockPos(3, 2, 1));

    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    fillCore(helper, new BlockPos(4, 1, 3));

    if (helper.getBlockEntity(new BlockPos(4, 1, 2)) instanceof BlockEntityCraftPipe pipe) {
      pipe.setPatternSlot(pipe.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    } else {
      helper.fail("no craft pipe");
    }

    helper.runAfterDelay(20, () -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table block entity");
        return;
      }
      table.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
      table.request(null, 8);
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table)) {
        helper.fail("no request table block entity");
        return;
      }
      int planks = 0;
      for (int i = 0; i < table.getItemStackHandler().getSlots(); i++) {
        ItemStack stack = table.getItemStackHandler().getStackInSlot(i);
        if (stack.is(Items.OAK_PLANKS)) {
          planks += stack.getCount();
        }
      }
      if (planks != 8) {
        helper.fail("expected 8 planks in the table buffer, found " + planks);
      }
      if (countIn(helper, new BlockPos(1, 1, 1), Items.OAK_LOG) != 0) {
        helper.fail("logs were not consumed from network storage");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void requestKeepsItemData(GameTestHelper helper) {
    buildRequestNetwork(helper, cell(1000));

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(cell(1000));
        table.request(null, 1);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      int fluid = fluidInTable(helper, new BlockPos(5, 1, 1));
      if (fluid < 0) {
        helper.fail("the cell has not arrived yet");
        return;
      }
      if (fluid != 1000) {
        helper.fail("the delivered cell lost its fluid: " + fluid + " mB");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void requestTellsDataVariantsApart(GameTestHelper helper) {
    buildRequestNetwork(helper, cell(1000), cell(500), cell(0));

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(cell(500));
        table.request(null, 1);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      int fluid = fluidInTable(helper, new BlockPos(5, 1, 1));
      if (fluid < 0) {
        helper.fail("the cell has not arrived yet");
        return;
      }
      if (fluid != 500) {
        helper.fail("expected exactly the 500 mB cell, got " + fluid + " mB");
        return;
      }

      if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof ChestBlockEntity chest)) {
        helper.fail("no provider chest");
        return;
      }
      int stillThere = 0;
      for (int i = 0; i < chest.getContainerSize(); i++) {
        if (chest.getItem(i).is(ModItems.FLUID_CELL)) {
          stillThere++;
        }
      }
      if (stillThere != 2) {
        helper.fail("the request took the wrong cells, " + stillThere + " left behind");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void machineRecipeTransformsInPlace(GameTestHelper helper) {
    buildRequestNetwork(helper, cell(0));
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());
    placeChest(helper, new BlockPos(4, 1, 3));

    if (helper.getBlockEntity(new BlockPos(4, 1, 2))
        instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe pipe) {
      int recipe = pipe.addRecipe();
      pipe.setIo(recipe, 0, cell(0));
      pipe.setIo(recipe, com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe.outputId(0),
          cell(1000));
    } else {
      helper.fail("no recipe pipe");
      return;
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(cell(1000));
        table.request(null, 1);
      } else {
        helper.fail("no request table");
      }
    });

    helper.runAfterDelay(120, () -> {
      if (helper.getBlockEntity(new BlockPos(4, 1, 3)) instanceof ChestBlockEntity machine) {
        for (int i = 0; i < machine.getContainerSize(); i++) {
          ItemStack stack = machine.getItem(i);
          if (stack.is(ModItems.FLUID_CELL)
              && com.faktocraft.common.item.base.FluidItem.getFluidAmount(stack) == 0) {
            machine.setItem(i, cell(1000));
            return;
          }
        }
        helper.fail("the recipe pipe never fed the machine an empty cell");
      }
    });

    helper.succeedWhen(() -> {
      int fluid = fluidInTable(helper, new BlockPos(5, 1, 1));
      if (fluid < 0) {
        helper.fail("the filled cell has not arrived yet");
        return;
      }
      if (fluid != 1000) {
        helper.fail("expected the filled cell, got " + fluid + " mB");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 900)
  public void fuelingStationFillsAndTheNetworkCollects(GameTestHelper helper) {
    buildRequestNetwork(helper, cell(0));
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());

    net.minecraft.world.level.block.state.BlockState stationState =
        com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.FUELING_STATION
            .defaultBlockState();
    if (com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.FUELING_STATION
        instanceof com.faktocraft.common.interfaces.block.IStateFacing facing) {
      stationState = facing.setDirection(stationState, net.minecraft.core.Direction.SOUTH);
    }
    helper.setBlock(new BlockPos(4, 1, 3), stationState);
    fillCore(helper, new BlockPos(4, 1, 3));
    if (helper.getBlockEntity(new BlockPos(4, 1, 3))
        instanceof com.faktocraft.common.block.impl.machines.fueling_station.BlockEntityFuelingStation station) {
      station.tank.fillFluid(new net.minecraftforge.fluids.FluidStack(
          net.minecraft.world.level.material.Fluids.WATER, 16000), 16000, false);
    } else {
      helper.fail("no fueling station");
      return;
    }

    if (helper.getBlockEntity(new BlockPos(4, 1, 2))
        instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe pipe) {
      int recipe = pipe.addRecipe();
      pipe.setIo(recipe, 0, cell(0));
      pipe.setIo(recipe, com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe.outputId(0),
          cell(1000));
    } else {
      helper.fail("no recipe pipe");
      return;
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable table) {
        table.setGhostTarget(cell(1000));
        table.request(null, 1);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      int fluid = fluidInTable(helper, new BlockPos(5, 1, 1));
      if (fluid == 1000) {
        return;
      }
      helper.fail("the filled cell never came back: table=" + fluid + " mB; " + stationState(helper));
    });
  }

  private static String stationState(GameTestHelper helper) {
    if (!(helper.getBlockEntity(new BlockPos(4, 1, 3))
        instanceof com.faktocraft.common.block.impl.machines.fueling_station.BlockEntityFuelingStation station)) {
      return "no station";
    }
    ItemStack inSlot = station.getItemStackHandler().getStackInSlot(0);
    StringBuilder out = new StringBuilder("");
    if (helper.getBlockEntity(new BlockPos(4, 1, 2))
        instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe pipe) {
      out.append("docked=").append(pipe.dockedPos()).append(" recipes=").append(pipe.recipeCount()).append(" ");
    }
    out.append("station slot=" + inSlot.getItem()
        + " x" + inSlot.getCount()
        + " fluid=" + com.faktocraft.common.item.base.FluidItem.getFluidAmount(inSlot)
        + " tank=" + station.tank.getFluidAmount()
        + " energy=" + station.getEnergyStorage().energyStored());
    if (helper.getBlockEntity(new BlockPos(3, 2, 1))
        instanceof com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController core) {
      for (com.faktocraft.common.block.impl.logistics.TaskLedger.TaskSummary task
          : core.getLedger().summaries()) {
        out.append(" task{").append(task.stateKey()).append(" ").append(task.detail())
            .append(" x").append(task.count()).append(" ").append(task.stack().getItem());
        for (com.faktocraft.common.block.impl.logistics.TaskLedger.SubRecord sub : task.subs()) {
          out.append(" |").append(sub.kind()).append(":").append(sub.stateKey())
              .append(" ").append(sub.item().getItem()).append(" x").append(sub.count());
        }
        out.append("}");
      }
      out.append(" decls=").append(core.collectDecls(core.graph()).size());
      out.append(" cfgErrors=").append(core.getConfigErrors());
      out.append(" stock=").append(com.faktocraft.common.block.impl.logistics.BlockEntityChassis
          .stockSnapshot(helper.getLevel(), core.graph(), core.getLedger()));
      for (com.faktocraft.common.block.impl.logistics.TaskLedger.HistoryRecord record
          : core.getLedger().userHistory()) {
        out.append(" [").append(record.stateKey()).append(" ").append(record.detail()).append("]");
      }
    }
    return out.toString();
  }

  private static void placeStation(GameTestHelper helper, BlockPos rel, net.minecraft.core.Direction facing) {
    net.minecraft.world.level.block.state.BlockState state =
        com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.FUELING_STATION
            .defaultBlockState();
    if (com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.FUELING_STATION
        instanceof com.faktocraft.common.interfaces.block.IStateFacing facing2) {
      state = facing2.setDirection(state, facing);
    }
    helper.setBlock(rel, state);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public void pipeDoesNotDockToTheMachineFront(GameTestHelper helper) {
    placeStation(helper, new BlockPos(2, 1, 2), net.minecraft.core.Direction.NORTH);
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());

    placeStation(helper, new BlockPos(5, 1, 2), net.minecraft.core.Direction.NORTH);
    helper.setBlock(new BlockPos(5, 1, 3), LogisticsRegistry.RECIPE_PIPE.defaultBlockState());

    helper.runAfterDelay(5, () -> {
      if (!(helper.getBlockEntity(new BlockPos(2, 1, 1))
          instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe atFront)
          || !(helper.getBlockEntity(new BlockPos(5, 1, 3))
              instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe atBack)) {
        helper.fail("recipe pipe missing");
        return;
      }
      if (atFront.dockedPos() != null) {
        helper.fail("a pipe on the machine front must not dock, got " + atFront.dockedPos());
        return;
      }
      if (!helper.absolutePos(new BlockPos(5, 1, 2)).equals(atBack.dockedPos())) {
        helper.fail("a pipe on any other face must dock to the station at "
            + helper.absolutePos(new BlockPos(5, 1, 2)) + ", got " + atBack.dockedPos());
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public void modulesStackInPacksButOnePerSlot(GameTestHelper helper) {
    ItemStack pack = new ItemStack(LogisticsRegistry.MODULE_PROVIDER, 16);
    if (pack.getMaxStackSize() != com.faktocraft.common.block.impl.logistics.ModuleItem.PACK) {
      helper.fail("a module pack should be " + com.faktocraft.common.block.impl.logistics.ModuleItem.PACK
          + ", got " + pack.getMaxStackSize());
      return;
    }
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    if (!(helper.getBlockEntity(new BlockPos(2, 1, 1)) instanceof BlockEntityChassis chassis)) {
      helper.fail("no chassis");
      return;
    }
    ItemStack leftover = chassis.getModules().insertItem(0, pack.copy(), false);
    if (chassis.getModules().getStackInSlot(0).getCount() != 1) {
      helper.fail("a module slot must keep a single module, holds "
          + chassis.getModules().getStackInSlot(0).getCount());
      return;
    }
    if (leftover.getCount() != 15) {
      helper.fail("the other 15 modules must come back, got " + leftover.getCount());
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public void toolboxTakesTheFieldTools(GameTestHelper helper) {
    for (net.minecraft.world.item.Item item : List.of(LogisticsRegistry.REMOTE_REQUESTER,
        com.faktocraft.common.registries.ModItems.PROSPECTOR)) {
      if (!com.faktocraft.common.item.impl.tools.ToolboxItem.isTool(new ItemStack(item))) {
        helper.fail(item + " should fit in the toolbox");
        return;
      }
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public void assemblyTableRunsOnLowVoltage(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());
    if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof BlockEntityAssemblyTable table)) {
      helper.fail("no assembly table");
      return;
    }
    com.faktocraft.common.enums.EnergyTier tier = table.getEnergyStorage().energyTier();
    if (tier != com.faktocraft.common.enums.EnergyTier.LOW) {
      helper.fail("the assembly table should be low voltage, got " + tier);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public void requestTableTurnsWithTheWrench(GameTestHelper helper) {
    BlockPos rel = new BlockPos(1, 1, 1);
    helper.setBlock(rel, LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    BlockPos pos = helper.absolutePos(rel);
    net.minecraft.world.level.block.state.BlockState state = helper.getLevel().getBlockState(pos);

    if (!(state.getBlock() instanceof com.faktocraft.common.interfaces.block.IStateFacing facing)) {
      helper.fail("the request table should carry a facing");
      return;
    }
    if (!com.faktocraft.common.util.wrench.WrenchHelper.hasAction(state.getBlock())) {
      helper.fail("the request table should answer the wrench");
      return;
    }

    helper.getLevel().setBlockAndUpdate(pos, facing.setDirection(state, net.minecraft.core.Direction.NORTH));
    net.minecraft.world.entity.player.Player player = helper.makeMockPlayer();
    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
        new ItemStack(ModItems.WRENCH));

    java.util.List<net.minecraft.core.Direction> seen = new java.util.ArrayList<>();
    for (int turn = 0; turn < 4; turn++) {
      net.minecraft.world.level.block.state.BlockState before = helper.getLevel().getBlockState(pos);
      if (!com.faktocraft.common.util.wrench.WrenchHelper.onWrenchUse(before, helper.getLevel(), pos,
          player, net.minecraft.core.Direction.UP)) {
        helper.fail("the wrench did nothing on turn " + turn);
        return;
      }
      seen.add(facing.getDirection(helper.getLevel().getBlockState(pos)));
    }

    java.util.List<net.minecraft.core.Direction> expected = java.util.List.of(
        net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.SOUTH,
        net.minecraft.core.Direction.WEST, net.minecraft.core.Direction.NORTH);
    if (!seen.equals(expected)) {
      helper.fail("the wrench should walk the table round the compass, got " + seen);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public void assemblyTableShowsAndSoundsWhileCrafting(GameTestHelper helper) {
    BlockPos tablePos = new BlockPos(4, 1, 3);
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.OAK_LOG, 2));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 2, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 2), LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(tablePos, LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());
    fillCore(helper, new BlockPos(3, 2, 1));
    fillCore(helper, tablePos);
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    if (!(helper.getBlockEntity(tablePos) instanceof BlockEntityAssemblyTable table)) {
      helper.fail("no assembly table");
      return;
    }
    if (table.getSoundEvent() == null) {
      helper.fail("the assembly table should have a working sound");
      return;
    }
    if (helper.getLevel().getBlockState(helper.absolutePos(tablePos))
        .getValue(com.faktocraft.common.util.BlockStateHelper.activeProperty)) {
      helper.fail("the assembly table should start idle");
      return;
    }

    if (helper.getBlockEntity(new BlockPos(4, 1, 2)) instanceof BlockEntityCraftPipe pipe) {
      pipe.setPatternSlot(pipe.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    } else {
      helper.fail("no craft pipe");
      return;
    }

    helper.runAfterDelay(20, () -> {
      if (helper.getBlockEntity(new BlockPos(5, 1, 1)) instanceof BlockEntityRequestTable requester) {
        requester.setGhostTarget(new ItemStack(Items.OAK_PLANKS));
        requester.request(null, 8);
      } else {
        helper.fail("no request table");
      }
    });

    helper.succeedWhen(() -> {
      if (!helper.getLevel().getBlockState(helper.absolutePos(tablePos))
          .getValue(com.faktocraft.common.util.BlockStateHelper.activeProperty)) {
        helper.fail("the assembly table never showed its working state");
      }
    });
  }

}
