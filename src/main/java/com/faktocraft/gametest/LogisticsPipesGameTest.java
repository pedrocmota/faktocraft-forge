package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.BlockEntityLogisticsController;
import com.faktocraft.common.block.impl.logistics.Endpoint;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class LogisticsPipesGameTest {

  private static final String TEMPLATE = "gametest_platform";

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
    if (helper.getBlockEntity(rel) instanceof FaktocraftBlockEntity be) {
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

  private static ItemStack overflowModule(net.minecraft.world.item.Item item) {
    ItemStack module = new ItemStack(item);
    ModuleSettings.setFlag(module, ModuleSettings.FLAG_OVERFLOW, true);
    return module;
  }

  private static ItemStack stickSink() {
    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setLine(sink, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(Items.STICK), "", false, false, 0));
    return sink;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void goldPipeShortensTheTrip(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof BlockEntityLogisticsController core)) {
      helper.fail("no core");
      return;
    }
    int tpp = Math.max(1, ModConfig.server().logistics_ticks_per_pipe);
    for (net.minecraft.world.level.block.Block pipe : new net.minecraft.world.level.block.Block[] {
        LogisticsRegistry.STONE_PIPE, LogisticsRegistry.GOLD_PIPE }) {
      List<BlockPos> route = new ArrayList<>();
      for (int x = 3; x <= 6; x++) {
        BlockPos rel = new BlockPos(x, 1, 1);
        helper.setBlock(rel, pipe.defaultBlockState());
        route.add(helper.absolutePos(rel));
      }
      var task = core.getLedger().createDelivery(new ItemStack(Items.STICK),
          Endpoint.inventory(route.get(0)), Endpoint.inventory(route.get(route.size() - 1)), route, 0);
      int expectedHop = pipe == LogisticsRegistry.GOLD_PIPE ? Math.max(1, tpp / 2) : tpp;
      int expected = Math.max(2, 3 * expectedHop);
      if (task.travelTicks() != expected) {
        helper.fail(pipe.getName().getString() + " trip cost " + task.travelTicks()
            + " ticks, expected " + expected);
        return;
      }
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public static void collectorModuleShipsGroundItemsToTheSink(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    placeChest(helper, new BlockPos(3, 1, 2));
    fillCore(helper, new BlockPos(4, 1, 1));
    putModule(helper, new BlockPos(1, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_COLLECTOR));
    putModule(helper, new BlockPos(3, 1, 1), 0, stickSink());
    putModule(helper, new BlockPos(3, 1, 1), 1, overflowModule(LogisticsRegistry.MODULE_DISPOSAL));

    BlockPos drop = helper.absolutePos(new BlockPos(1, 2, 1));
    helper.getLevel().addFreshEntity(new ItemEntity(helper.getLevel(),
        drop.getX() + 0.5, drop.getY() + 0.5, drop.getZ() + 0.5, new ItemStack(Items.STICK, 3), 0, 0, 0));

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(3, 1, 2), Items.STICK) != 3) {
        helper.fail("the sticks never reached the sink chest");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public static void disposalModuleSwallowsWhatNoSinkTakes(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.STICK, 8));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 2), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 3), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    fillCore(helper, new BlockPos(3, 1, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));
    putModule(helper, new BlockPos(2, 1, 3), 0, overflowModule(LogisticsRegistry.MODULE_DISPOSAL));

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(1, 1, 1), Items.STICK) != 0) {
        helper.fail("the disposal module never drained the chest");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public static void ejectorModuleDropsWhatNoSinkTakes(GameTestHelper helper) {
    placeChest(helper, new BlockPos(1, 1, 1), new ItemStack(Items.STICK, 8));
    helper.setBlock(new BlockPos(2, 1, 1), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 1), LogisticsRegistry.LOGISTICS_CONTROLLER.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 2), LogisticsRegistry.STONE_PIPE.defaultBlockState());
    helper.setBlock(new BlockPos(2, 1, 3), LogisticsRegistry.CHASSIS_1.defaultBlockState());
    fillCore(helper, new BlockPos(3, 1, 1));
    putModule(helper, new BlockPos(2, 1, 1), 0, new ItemStack(LogisticsRegistry.MODULE_EXTRACTOR));
    putModule(helper, new BlockPos(2, 1, 3), 0, overflowModule(LogisticsRegistry.MODULE_EJECTOR));

    helper.succeedWhen(() -> {
      if (countIn(helper, new BlockPos(1, 1, 1), Items.STICK) != 0) {
        helper.fail("the ejector module never drained the chest");
        return;
      }
      AABB around = new AABB(helper.absolutePos(new BlockPos(2, 1, 3))).inflate(2);
      int dropped = 0;
      for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, around)) {
        if (entity.getItem().is(Items.STICK)) {
          dropped += entity.getItem().getCount();
        }
      }
      if (dropped != 8) {
        helper.fail("expected 8 ejected sticks on the floor, found " + dropped);
      }
    });
  }
}
