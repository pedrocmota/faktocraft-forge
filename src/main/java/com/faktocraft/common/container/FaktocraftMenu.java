package com.faktocraft.common.container;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.entity.slot.SlotUpgrade;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.ArrayList;

public abstract class FaktocraftMenu extends AbstractContainerMenu {

  private final FaktocraftBlockEntity blockEntity;
  private final Player viewer;
  private long lastGuiSyncSent = Long.MIN_VALUE;
  private final ArrayList<MachineSlot> machineSlots = new ArrayList<>();
  private final ArrayList<SlotElectricMenu> batterySlots = new ArrayList<>();
  private final ArrayList<SlotUpgradeMenu> upgradeSlots = new ArrayList<>();
  private int playerInventoryStart = -1;

  public int playerInvLeft = 8;
  public int playerInvTop = 84;

  public static final int BUTTON_REDSTONE_TOGGLE = 947;
  public static final int BUTTON_PRIORITY_CYCLE = 948;

  private final net.minecraft.world.inventory.DataSlot redstoneOnlyData;
  private final net.minecraft.world.inventory.DataSlot priorityModeData;
  private final net.minecraft.world.inventory.DataSlot priorityDefaultData;

  protected FaktocraftMenu(MenuType<?> menuType, int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    super(menuType, windowId);
    this.blockEntity = (FaktocraftBlockEntity) level.getBlockEntity(pos);
    this.viewer = player;
    this.redstoneOnlyData = addDataSlot(blockEntity != null ? new net.minecraft.world.inventory.DataSlot() {
      @Override
      public int get() {
        return blockEntity.isRedstoneOnly() ? 1 : 0;
      }

      @Override
      public void set(int value) {
        blockEntity.setRedstoneOnly(value == 1);
      }
    } : net.minecraft.world.inventory.DataSlot.standalone());
    this.priorityModeData = addDataSlot(blockEntity != null ? new net.minecraft.world.inventory.DataSlot() {
      @Override
      public int get() {
        return blockEntity.getGeneratorPriorityMode();
      }

      @Override
      public void set(int value) {
        blockEntity.setGeneratorPriorityMode(value);
      }
    } : net.minecraft.world.inventory.DataSlot.standalone());
    this.priorityDefaultData = addDataSlot(blockEntity != null ? new net.minecraft.world.inventory.DataSlot() {
      @Override
      public int get() {
        return blockEntity.defaultGeneratorPriority();
      }

      @Override
      public void set(int value) {
      }
    } : net.minecraft.world.inventory.DataSlot.standalone());
  }

  public boolean isRedstoneOnly() {
    return redstoneOnlyData.get() == 1;
  }

  public int getGeneratorPriorityMode() {
    return priorityModeData.get();
  }

  public int getGeneratorPriorityDefault() {
    return priorityDefaultData.get();
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    if (blockEntity != null && !blockEntity.isRemoved()
        && viewer instanceof net.minecraft.server.level.ServerPlayer serverPlayer
        && blockEntity.getGuiChangeTick() > lastGuiSyncSent) {
      lastGuiSyncSent = blockEntity.getGuiChangeTick();
      var packet = blockEntity.getUpdatePacket();
      if (packet != null) {
        serverPlayer.connection.send(packet);
      }
    }
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (id == BUTTON_REDSTONE_TOGGLE && blockEntity != null && blockEntity.supportsRedstoneControl()) {
      blockEntity.setRedstoneOnly(!blockEntity.isRedstoneOnly());
      return true;
    }
    if (id == BUTTON_PRIORITY_CYCLE && blockEntity != null && blockEntity.isGenerator()) {
      blockEntity.setGeneratorPriorityMode((blockEntity.getGeneratorPriorityMode() + 1) % 6);
      return true;
    }
    return super.clickMenuButton(player, id);
  }

  public void init(Inventory playerInventory) {
    if (blockEntity == null) {
      return;
    }

    if (blockEntity.hasInventory()) {
      for (FaktocraftSlot slot : blockEntity.getSlots()) {
        MachineSlot menuSlot = switch (slot.getInventorySlotType()) {
          case OUTPUT, BONUS -> new SlotOutput(blockEntity, blockEntity.getItemStackHandler(), slot);
          case DISABLED -> new SlotDisabled(blockEntity, blockEntity.getItemStackHandler(), slot);
          default -> new MachineSlot(blockEntity, blockEntity.getItemStackHandler(), slot);
        };
        machineSlots.add(menuSlot);
        addSlot(menuSlot);
      }
    }

    if (blockEntity.hasBattery()) {
      for (IElectricSlot slot : blockEntity.getElectricSlot()) {
        SlotElectricMenu menuSlot = new SlotElectricMenu(blockEntity, blockEntity.getBatteryStackHandler(), slot);
        batterySlots.add(menuSlot);
        addSlot(menuSlot);
      }
    }

    if (blockEntity.hasUpgrades() && blockEntity.getUpgradeStackHandler() != null) {
      for (SlotUpgrade slot : blockEntity.getUpgradeSlot()) {
        SlotUpgradeMenu menuSlot = new SlotUpgradeMenu(blockEntity, blockEntity.getUpgradeStackHandler(), slot);
        upgradeSlots.add(menuSlot);
        addSlot(menuSlot);
      }
    }

    layoutPlayerInventorySlots(playerInventory, playerInvLeft, playerInvTop);
  }

  public int playerInventoryStart() {
    return playerInventoryStart >= 0 ? playerInventoryStart : Math.max(0, slots.size() - 36);
  }

  protected void layoutPlayerInventorySlots(Inventory playerInventory, int leftCol, int topRow) {
    playerInventoryStart = slots.size();
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        addSlot(new Slot(playerInventory, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
      }
    }
    for (int col = 0; col < 9; col++) {
      addSlot(new Slot(playerInventory, col, leftCol + col * 18, topRow + 58));
    }
  }

  public FaktocraftBlockEntity getBlockEntity() {
    return blockEntity;
  }

  @Override
  public boolean stillValid(Player player) {
    if (blockEntity == null || blockEntity.getLevel() == null) {
      return false;
    }
    return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player,
        blockEntity.getBlockState().getBlock());
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack result = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot.hasItem()) {
      ItemStack stack = slot.getItem();
      result = stack.copy();

      int machineSlotCount = machineSlots.size() + batterySlots.size() + upgradeSlots.size();
      int totalSlots = machineSlotCount + 36;

      if (index < machineSlotCount) {
        if (!moveItemStackTo(stack, machineSlotCount, totalSlots, true)) {
          return ItemStack.EMPTY;
        }
      } else {

        int dockStart = machineSlots.size();
        boolean bayItem = false;
        for (int i = dockStart; i < machineSlotCount; i++) {
          if (this.slots.get(i).mayPlace(stack)) {
            bayItem = true;
            break;
          }
        }
        if (bayItem) {
          if (!moveItemStackTo(stack, dockStart, machineSlotCount, false)) {
            return ItemStack.EMPTY;
          }
        } else if (!moveItemStackTo(stack, 0, dockStart, false)) {
          return ItemStack.EMPTY;
        }
      }

      if (stack.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }

      if (stack.getCount() == result.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(player, stack);
    }
    return result;
  }
}
