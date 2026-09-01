package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class MenuChassis extends AbstractContainerMenu {

  public static final int BUTTON_CONFIGURE_BASE = 100;

  public static final int WIDTH = 176;
  public static final int MODULE_Y = 33;
  public static final int UPGRADE_X = 178;
  public static final int UPGRADE_Y = 9;
  public static final int INVENTORY_Y = 84;

  public static int rowLeft(int count) {
    return (WIDTH - count * 18) / 2;
  }

  private final BlockEntityChassis chassis;

  public MenuChassis(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(LogisticsRegistry.CHASSIS_MENU, windowId);
    this.chassis = level.getBlockEntity(pos) instanceof BlockEntityChassis found ? found : null;

    if (chassis != null) {
      int moduleSlots = chassis.moduleSlotCount();
      for (int i = 0; i < moduleSlots; i++) {
        addSlot(new Slot(chassis.getModules(), i, rowLeft(moduleSlots) + 1 + i * 18, MODULE_Y) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ModuleItem;
          }

          @Override
          public int getMaxStackSize() {
            return 1;
          }
        });
      }
      int upgradeSlots = chassis.upgradeSlotCount();
      for (int i = 0; i < upgradeSlots; i++) {
        addSlot(new Slot(chassis.getUpgrades(), i, UPGRADE_X, UPGRADE_Y + i * 18) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ThroughputUpgradeItem;
          }

          @Override
          public int getMaxStackSize() {
            return 1;
          }
        });
      }
    }

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
      }
    }
    for (int col = 0; col < 9; col++) {
      addSlot(new Slot(playerInventory, col, 8 + col * 18, INVENTORY_Y + 58));
    }
  }

  public BlockEntityChassis getChassis() {
    return chassis;
  }

  public int moduleSlotCount() {
    return chassis != null ? chassis.moduleSlotCount() : 0;
  }

  public int upgradeSlotCount() {
    return chassis != null ? chassis.upgradeSlotCount() : 0;
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (chassis == null || !(player instanceof ServerPlayer serverPlayer)) {
      return false;
    }
    int slot = id - BUTTON_CONFIGURE_BASE;
    if (slot < 0 || slot >= chassis.moduleSlotCount()) {
      return false;
    }
    ItemStack module = chassis.getModules().getStackInSlot(slot);
    if (!(module.getItem() instanceof ModuleItem)) {
      return false;
    }
    BlockPos pos = chassis.getBlockPos();
    NetworkHooks.openScreen(serverPlayer,
        new net.minecraft.world.SimpleMenuProvider(
            (windowId, inventory, p) -> new MenuModule(windowId, p.level(), pos, slot, inventory, p),
            Component.translatable(module.getDescriptionId())),
        buf -> {
          buf.writeBlockPos(pos);
          buf.writeVarInt(slot);
        });
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (chassis == null) {
      return ItemStack.EMPTY;
    }
    int machineSlots = chassis.moduleSlotCount() + chassis.upgradeSlotCount();
    Slot slot = this.slots.get(index);
    if (!slot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = slot.getItem();
    ItemStack original = stack.copy();
    if (index < machineSlots) {
      if (!moveItemStackTo(stack, machineSlots, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else if (stack.getItem() instanceof ModuleItem) {
      if (!moveItemStackTo(stack, 0, chassis.moduleSlotCount(), false)) {
        return ItemStack.EMPTY;
      }
    } else if (stack.getItem() instanceof ThroughputUpgradeItem) {
      if (!moveItemStackTo(stack, chassis.moduleSlotCount(), machineSlots, false)) {
        return ItemStack.EMPTY;
      }
    } else {
      return ItemStack.EMPTY;
    }
    if (stack.isEmpty()) {
      slot.set(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }
    return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
  }

  @Override
  public boolean stillValid(Player player) {
    return chassis != null && !chassis.isRemoved()
        && player.distanceToSqr(chassis.getBlockPos().getCenter()) <= 64.0;
  }
}
