package com.faktocraft.common.block.impl.forester;

import com.faktocraft.common.block.impl.pipe.PipeExtractor;
import com.faktocraft.common.block.impl.quarry.BlockEntityGantry;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.item.impl.CapacitorItem;
import com.faktocraft.common.item.impl.upgrade.TensionUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MenuForester extends AbstractContainerMenu {

  public static final int DOCK_SLOTS = 8;
  public static final int INPUT_START = DOCK_SLOTS;
  public static final int SAPLING_END = INPUT_START + BlockEntityForester.SAPLING_SLOTS;
  public static final int INPUT_END = INPUT_START + BlockEntityForester.INPUT_SLOTS;
  public static final int MACHINE_SLOTS = INPUT_END + BlockEntityGantry.INVENTORY_SLOTS;
  public static final int BUTTON_RESIN = 3;

  public static final int SAPLING_X = 98;
  public static final int FERTILIZER_X = 120;
  public static final int INPUT_Y = 17;

  private final BlockEntityForester forester;
  private final DataSlot runMode;
  private final DataSlot status;
  private final DataSlot resinMode;

  public BlockEntityForester getForester() {
    return forester;
  }

  public MenuForester(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(ForesterRegistry.FORESTER_MENU, windowId);
    this.forester = level.getBlockEntity(pos) instanceof BlockEntityForester found ? found : null;

    if (forester != null) {
      for (int i = 0; i < 2; i++) {
        addSlot(new Slot(forester.getBatteryStackHandler(), i, -20, 9 + i * 18) {
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
      addSlot(new Slot(forester.getBatteryStackHandler(), FaktocraftBlockEntity.TENSION_DOCK_SLOT, -20, 45) {
        @Override
        public boolean mayPlace(ItemStack stack) {
          return stack.getItem() instanceof TensionUpgrade;
        }

        @Override
        public int getMaxStackSize() {
          return 1;
        }
      });
      for (int i = 0; i < BlockEntityGantry.UPGRADE_SLOTS; i++) {
        addSlot(new Slot(forester.getUpgrades(), i, 178, 9 + i * 18) {
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
      addSlot(new Slot(forester.getBatteryStackHandler(), 3, -20, 63) {
        @Override
        public boolean mayPlace(ItemStack stack) {
          return stack.getItem() instanceof ElectricItem;
        }

        @Override
        public int getMaxStackSize() {
          return 1;
        }
      });

      for (int i = 0; i < BlockEntityForester.SAPLING_SLOTS; i++) {
        addSlot(new Slot(forester.getInputs(), i, SAPLING_X, INPUT_Y + i * 18) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return BlockEntityForester.isSapling(stack);
          }
        });
      }
      for (int i = 0; i < BlockEntityForester.FERTILIZER_SLOTS; i++) {
        addSlot(new Slot(forester.getInputs(), BlockEntityForester.SAPLING_SLOTS + i, FERTILIZER_X,
            INPUT_Y + i * 18) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return BlockEntityForester.isFertilizer(stack);
          }
        });
      }

      for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 9; col++) {
          addSlot(new Slot(forester.getInventory(), col + row * 9, 8 + col * 18, 75 + row * 18) {
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

    this.runMode = addDataSlot(forester != null ? new DataSlot() {
      @Override
      public int get() {
        return forester.getRunMode();
      }

      @Override
      public void set(int value) {
        forester.setRunMode(value);
      }
    } : DataSlot.standalone());
    this.status = addDataSlot(forester != null ? new DataSlot() {
      @Override
      public int get() {
        return forester.getStatus();
      }

      @Override
      public void set(int value) {
        forester.setStatusClient(value);
      }
    } : DataSlot.standalone());
    this.resinMode = addDataSlot(forester != null ? new DataSlot() {
      @Override
      public int get() {
        return forester.isResinMode() ? 1 : 0;
      }

      @Override
      public void set(int value) {
        forester.setResinMode(value != 0);
      }
    } : DataSlot.standalone());
  }

  public int getRunMode() {
    return runMode.get();
  }

  public int getStatus() {
    return status.get();
  }

  public boolean isResinMode() {
    return resinMode.get() != 0;
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (forester == null) {
      return false;
    }
    if (id >= 0 && id <= 2) {
      forester.setRunMode(id);
      return true;
    }
    if (id == BUTTON_RESIN) {
      forester.setResinMode(!forester.isResinMode());
      return true;
    }
    return false;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (forester == null) {
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
    } else if (BlockEntityForester.isSapling(stack)) {
      if (!moveItemStackTo(stack, INPUT_START, SAPLING_END, false)) {
        return ItemStack.EMPTY;
      }
    } else if (BlockEntityForester.isFertilizer(stack)) {
      if (!moveItemStackTo(stack, SAPLING_END, INPUT_END, false)) {
        return ItemStack.EMPTY;
      }
    } else if (stack.getItem() instanceof TensionUpgrade) {
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
    return forester != null && !forester.isRemoved()
        && player.distanceToSqr(forester.getBlockPos().getCenter()) <= 64.0;
  }
}
