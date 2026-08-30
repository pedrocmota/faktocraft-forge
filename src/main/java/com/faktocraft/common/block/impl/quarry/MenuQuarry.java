package com.faktocraft.common.block.impl.quarry;

import com.faktocraft.common.block.impl.pipe.PipeExtractor;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.item.impl.CapacitorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MenuQuarry extends AbstractContainerMenu {

  public static final int DOCK_SLOTS = 8;
  public static final int MACHINE_SLOTS = DOCK_SLOTS + BlockEntityQuarry.INVENTORY_SLOTS;

  private final BlockEntityQuarry quarry;

  public BlockEntityQuarry getQuarry() {
    return quarry;
  }

  public MenuQuarry(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(QuarryRegistry.QUARRY_MENU, windowId);
    this.quarry = level.getBlockEntity(pos) instanceof BlockEntityQuarry found ? found : null;

    if (quarry != null) {
      for (int i = 0; i < 2; i++) {
        addSlot(new Slot(quarry.getBatteryStackHandler(), i, -20, 9 + i * 18) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof CapacitorItem;
          }

          @Override
          public int getMaxStackSize() {
            return 1;
          }
        });
      }
      addSlot(new Slot(quarry.getBatteryStackHandler(),
          com.faktocraft.common.entity.block.IndRebBlockEntity.TENSION_DOCK_SLOT, -20, 45) {
        @Override
        public boolean mayPlace(ItemStack stack) {
          return stack.getItem() instanceof com.faktocraft.common.item.impl.upgrade.TensionUpgrade;
        }

        @Override
        public int getMaxStackSize() {
          return 1;
        }
      });
      for (int i = 0; i < BlockEntityQuarry.UPGRADE_SLOTS; i++) {
        addSlot(new Slot(quarry.getUpgrades(), i, 178, 9 + i * 18) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return PipeExtractor.isMotorUpgrade(stack);
          }

          @Override
          public int getMaxStackSize() {
            return 1;
          }
        });
      }
      addSlot(new Slot(quarry.getBatteryStackHandler(), 3, -20, 63) {
        @Override
        public boolean mayPlace(ItemStack stack) {
          return stack.getItem() instanceof ElectricItem;
        }

        @Override
        public int getMaxStackSize() {
          return 1;
        }
      });

      for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 9; col++) {
          addSlot(new Slot(quarry.getInventory(), col + row * 9, 8 + col * 18, 75 + row * 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
              return false;
            }
          });
        }
      }
    }

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 141 + row * 18));
      }
    }
    for (int col = 0; col < 9; col++) {
      addSlot(new Slot(playerInventory, col, 8 + col * 18, 199));
    }

    this.runMode = addDataSlot(quarry != null ? new net.minecraft.world.inventory.DataSlot() {
      @Override
      public int get() {
        return quarry.getRunMode();
      }

      @Override
      public void set(int value) {
        quarry.setRunMode(value);
      }
    } : net.minecraft.world.inventory.DataSlot.standalone());
    this.status = addDataSlot(quarry != null ? new net.minecraft.world.inventory.DataSlot() {
      @Override
      public int get() {
        return quarry.getStatus();
      }

      @Override
      public void set(int value) {
        quarry.setStatusClient(value);
      }
    } : net.minecraft.world.inventory.DataSlot.standalone());
  }

  private final net.minecraft.world.inventory.DataSlot runMode;
  private final net.minecraft.world.inventory.DataSlot status;

  public int getRunMode() {
    return runMode.get();
  }

  public int getStatus() {
    return status.get();
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (quarry != null && id >= 0 && id <= 2) {
      quarry.setRunMode(id);
      return true;
    }
    return false;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (quarry == null) {
      return ItemStack.EMPTY;
    }
    Slot slot = this.slots.get(index);
    if (!slot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = slot.getItem();
    ItemStack original = stack.copy();
    if (index < MACHINE_SLOTS) {
      if (!moveItemStackTo(stack, MACHINE_SLOTS, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else if (stack.getItem() instanceof com.faktocraft.common.item.impl.upgrade.TensionUpgrade) {
      if (!moveItemStackTo(stack, 2, 3, false)) {
        return ItemStack.EMPTY;
      }
    } else if (PipeExtractor.isMotorUpgrade(stack)) {
      if (!moveItemStackTo(stack, 3, 7, false)) {
        return ItemStack.EMPTY;
      }
    } else if (stack.getItem() instanceof CapacitorItem) {
      if (!moveItemStackTo(stack, 0, 2, false)) {
        return ItemStack.EMPTY;
      }
    } else if (stack.getItem() instanceof ElectricItem) {
      if (!moveItemStackTo(stack, 7, DOCK_SLOTS, false)) {
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
    return quarry != null && !quarry.isRemoved()
        && player.distanceToSqr(quarry.getBlockPos().getCenter()) <= 64.0;
  }
}
