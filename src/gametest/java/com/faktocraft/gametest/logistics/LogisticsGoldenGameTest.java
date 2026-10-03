package com.faktocraft.gametest.logistics;

import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class LogisticsGoldenGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final String EXPECTED = "S{}[] B{cobblestone=8}[0=cobblestonex8] C{iron_ingot=27}[0=iron_ingotx27]"
      + " D{}[] E{cobblestone=12, oak_planks=10}[0=cobblestonex12, 1=oak_planksx10]";

  private static final BlockPos SOURCE = new BlockPos(1, 1, 1);
  private static final BlockPos CHASSIS_A = new BlockPos(2, 1, 1);
  private static final BlockPos CHASSIS_B = new BlockPos(4, 1, 1);
  private static final BlockPos CHEST_B = new BlockPos(4, 1, 2);
  private static final BlockPos CHASSIS_C = new BlockPos(6, 1, 1);
  private static final BlockPos CHEST_C = new BlockPos(6, 1, 2);
  private static final BlockPos CHASSIS_D = new BlockPos(7, 1, 1);
  private static final BlockPos CHEST_D = new BlockPos(7, 1, 2);
  private static final BlockPos CHASSIS_E = new BlockPos(8, 1, 1);
  private static final BlockPos CHEST_E = new BlockPos(8, 1, 2);
  private static final BlockPos CORE = new BlockPos(2, 2, 1);

  private static void placeChest(GameTestHelper helper, BlockPos rel, ItemStack... stacks) {
    helper.setBlock(rel, Blocks.CHEST.defaultBlockState());
    if (TestUtil.blockEntity(helper, rel) instanceof ChestBlockEntity chest) {
      for (int i = 0; i < stacks.length; i++) {
        chest.setItem(i, stacks[i]);
      }
    }
  }

  private static void putModule(GameTestHelper helper, BlockPos rel, int slot, ItemStack module) {
    if (TestUtil.blockEntity(helper, rel) instanceof BlockEntityChassis chassis) {
      chassis.getModules().setStackInSlot(slot, module);
    } else {
      helper.fail("no chassis at " + rel);
    }
  }

  private static ItemStack sink(ItemStack filter, int priority, boolean overflow) {
    ItemStack module = new ItemStack(LogisticsRegistry.MODULE_SINK);
    if (filter != null) {
      ModuleSettings.setLine(module, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM, filter, "",
          false, false, 0));
    }
    ModuleSettings.setPriority(module, priority);
    if (overflow) {
      ModuleSettings.setFlag(module, ModuleSettings.FLAG_OVERFLOW, true);
    }
    return module;
  }

  private static String chestSignature(GameTestHelper helper, String name, BlockPos rel) {
    Map<String, Integer> counts = new TreeMap<>();
    List<String> slots = new ArrayList<>();
    if (TestUtil.blockEntity(helper, rel) instanceof ChestBlockEntity chest) {
      for (int i = 0; i < chest.getContainerSize(); i++) {
        ItemStack stack = chest.getItem(i);
        if (!stack.isEmpty()) {
          String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
          counts.merge(id, stack.getCount(), Integer::sum);
          slots.add(i + "=" + id + "x" + stack.getCount());
        }
      }
    }
    return name + counts + slots;
  }

  private static String signature(GameTestHelper helper) {
    return chestSignature(helper, "S", SOURCE) + " " + chestSignature(helper, "B", CHEST_B) + " "
        + chestSignature(helper, "C", CHEST_C) + " " + chestSignature(helper, "D", CHEST_D) + " "
        + chestSignature(helper, "E", CHEST_E);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 500)
  public static void sortingAndSupplyingIsStable(GameTestHelper helper) {
    placeChest(helper, SOURCE, new ItemStack(Items.IRON_INGOT, 20), new ItemStack(Items.COBBLESTONE, 20),
        new ItemStack(Items.OAK_PLANKS, 10), new ItemStack(Items.IRON_INGOT, 7));
    helper.setBlock(CHASSIS_A, LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(CHASSIS_B, LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(5, 1, 1), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(CHASSIS_C, LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(CHASSIS_D, LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(CHASSIS_E, LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(CORE, LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    placeChest(helper, CHEST_B);
    placeChest(helper, CHEST_C);
    placeChest(helper, CHEST_D);
    placeChest(helper, CHEST_E);

    if (TestUtil.blockEntity(helper, CORE) instanceof FaktocraftBlockEntity core) {
      core.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.BASIC_CAPACITOR));
      core.getEnergyStorage().setEnergy(core.getEnergyStorage().maxEnergy());
    }

    putModule(helper, CHASSIS_A, 0, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));
    putModule(helper, CHASSIS_A, 1, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    ItemStack supplier = new ItemStack(LogisticsRegistry.MODULE_SUPPLIER);
    ModuleSettings.setLine(supplier, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.COBBLESTONE), "", false, false, 8));
    putModule(helper, CHASSIS_B, 0, supplier);

    putModule(helper, CHASSIS_C, 0, sink(new ItemStack(Items.IRON_INGOT), 1, false));
    putModule(helper, CHASSIS_D, 0, sink(new ItemStack(Items.IRON_INGOT), 0, false));
    putModule(helper, CHASSIS_E, 0, sink(null, 0, true));
    putModule(helper, CHASSIS_E, 1, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    helper.runAfterDelay(400, () -> {
      String actual = signature(helper);
      if (!EXPECTED.equals(actual)) {
        helper.fail("golden mismatch: " + actual);
        return;
      }
      helper.succeed();
    });
  }
}
