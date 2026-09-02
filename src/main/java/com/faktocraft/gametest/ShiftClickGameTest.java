package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.recycler.MenuRecycler;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@net.minecraftforge.gametest.GameTestHolder(Faktocraft.MODID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class ShiftClickGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static int menuIndexOfPlayerSlot(MenuRecycler menu, Player player, int invSlot) {
    for (int i = 0; i < menu.slots.size(); i++) {
      Slot slot = menu.slots.get(i);
      if (slot.container == player.getInventory() && slot.getSlotIndex() == invSlot) {
        return i;
      }
    }
    return -1;
  }

  private static boolean handlerHolds(com.faktocraft.common.util.ItemStackHandler handler, Item item) {
    for (int i = 0; i < handler.getSlots(); i++) {
      if (handler.getStackInSlot(i).is(item)) {
        return true;
      }
    }
    return false;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void shiftClickPrefersTheSideBays(GameTestHelper helper) {
    BlockPos rel = new BlockPos(1, 1, 1);
    helper.setBlock(rel, M2Registry.RECYCLER.defaultBlockState());
    if (!(helper.getBlockEntity(rel) instanceof FaktocraftBlockEntity machine)) {
      helper.fail("no recycler block entity");
      return;
    }
    Player player = helper.makeMockPlayer();
    player.getInventory().setItem(9, new ItemStack(ModItems.CRUDE_CAPACITOR, 3));
    player.getInventory().setItem(10, new ItemStack(ModItems.OVERCLOCKER_UPGRADE));
    MenuRecycler menu = new MenuRecycler(1, helper.getLevel(),
        helper.absolutePos(rel), player.getInventory(), player);

    int capacitorIndex = menuIndexOfPlayerSlot(menu, player, 9);
    menu.quickMoveStack(player, capacitorIndex);
    menu.quickMoveStack(player, capacitorIndex);
    menu.quickMoveStack(player, capacitorIndex);
    if (!handlerHolds(machine.getBatteryStackHandler(), ModItems.CRUDE_CAPACITOR)) {
      helper.fail("the capacitor never reached the dock");
      return;
    }
    if (handlerHolds(machine.getItemStackHandler(), ModItems.CRUDE_CAPACITOR)) {
      helper.fail("a capacitor landed in the machine inventory");
      return;
    }
    if (player.getInventory().getItem(9).getCount() != 1) {
      helper.fail("expected the leftover capacitor to stay in hand, found "
          + player.getInventory().getItem(9).getCount());
      return;
    }

    menu.quickMoveStack(player, menuIndexOfPlayerSlot(menu, player, 10));
    if (!handlerHolds(machine.getUpgradeStackHandler(), ModItems.OVERCLOCKER_UPGRADE)) {
      helper.fail("the upgrade never reached the upgrade bay");
      return;
    }
    if (handlerHolds(machine.getItemStackHandler(), ModItems.OVERCLOCKER_UPGRADE)) {
      helper.fail("an upgrade landed in the machine inventory");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void assemblyTableKeepsCapacitorsOutOfTheIngredients(GameTestHelper helper) {
    BlockPos rel = new BlockPos(1, 1, 1);
    helper.setBlock(rel,
        com.faktocraft.common.block.impl.logistics.LogisticsRegistry.ASSEMBLY_TABLE.defaultBlockState());
    if (!(helper
        .getBlockEntity(rel) instanceof com.faktocraft.common.block.impl.logistics.BlockEntityAssemblyTable table)) {
      helper.fail("no assembly table");
      return;
    }
    Player player = helper.makeMockPlayer();
    player.getInventory().setItem(9, new ItemStack(ModItems.CRUDE_CAPACITOR, 3));
    var menu = new com.faktocraft.common.block.impl.logistics.MenuAssemblyTable(1, helper.getLevel(),
        helper.absolutePos(rel), player.getInventory(), player);

    int capacitorIndex = -1;
    for (int i = 0; i < menu.slots.size(); i++) {
      Slot slot = menu.slots.get(i);
      if (slot.container == player.getInventory() && slot.getSlotIndex() == 9) {
        capacitorIndex = i;
        break;
      }
    }
    menu.quickMoveStack(player, capacitorIndex);
    menu.quickMoveStack(player, capacitorIndex);
    menu.quickMoveStack(player, capacitorIndex);
    if (!handlerHolds(table.getBatteryStackHandler(), ModItems.CRUDE_CAPACITOR)) {
      helper.fail("the capacitor never reached the table's dock");
      return;
    }
    if (handlerHolds(table.getIngredients(), ModItems.CRUDE_CAPACITOR)) {
      helper.fail("a capacitor leaked into the ingredient buffer");
      return;
    }
    if (player.getInventory().getItem(9).getCount() != 1) {
      helper.fail("expected the leftover capacitor to stay in hand, found "
          + player.getInventory().getItem(9).getCount());
      return;
    }
    helper.succeed();
  }
}
