package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MenuExtractorPipe extends AbstractContainerMenu {

  public static final int UPGRADE_X = 178;
  public static final int UPGRADE_Y = 9;
  public static final int DOCK_X = -20;
  public static final int DOCK_Y = 9;

  public static final int MACHINE_SLOTS = PipeExtractor.UPGRADE_SLOTS + PipeExtractor.DOCK_SLOTS;
  public static final int DOCK_INDEX = PipeExtractor.UPGRADE_SLOTS;

  private static final int SYNC_BITS = 12;
  private static final int SYNC_MASK = (1 << SYNC_BITS) - 1;

  private final BlockEntity blockEntity;
  private final PipeExtractor extractor;
  private final DataSlot runMode;
  private int syncLo;
  private int syncHi;

  public MenuExtractorPipe(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(PipeRegistry.EXTRACTOR_PIPE_MENU, windowId);
    this.blockEntity = level.getBlockEntity(pos);
    this.extractor = blockEntity instanceof IExtractorPipe pipe ? pipe.extractor() : null;

    if (extractor != null) {
      for (int i = 0; i < PipeExtractor.UPGRADE_SLOTS; i++) {
        addSlot(new Slot(extractor.getUpgrades(), i, UPGRADE_X, UPGRADE_Y + i * 18) {
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
      for (int i = 0; i < PipeExtractor.DOCK_SLOTS; i++) {
        final int dockSlot = i;
        addSlot(new Slot(extractor.getDock(), i, DOCK_X, DOCK_Y + i * 18) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return mayDock(dockSlot, stack);
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
        addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
      }
    }
    for (int col = 0; col < 9; col++) {
      addSlot(new Slot(playerInventory, col, 8 + col * 18, 166));
    }

    this.runMode = addDataSlot(extractor != null ? new DataSlot() {
      @Override
      public int get() {
        return extractor.getRunMode();
      }

      @Override
      public void set(int value) {
        extractor.setRunMode(value);
      }
    } : DataSlot.standalone());

    addDataSlot(extractor != null ? new DataSlot() {
      @Override
      public int get() {
        return extractor.energy().energyStored() & SYNC_MASK;
      }

      @Override
      public void set(int value) {
        syncLo = value;
        extractor.energy().setEnergy((syncHi << SYNC_BITS) | syncLo);
      }
    } : DataSlot.standalone());
    addDataSlot(extractor != null ? new DataSlot() {
      @Override
      public int get() {
        return extractor.energy().energyStored() >> SYNC_BITS;
      }

      @Override
      public void set(int value) {
        syncHi = value;
        extractor.energy().setEnergy((syncHi << SYNC_BITS) | syncLo);
      }
    } : DataSlot.standalone());
  }

  private boolean mayDock(int dockSlot, ItemStack stack) {
    if (extractor == null) {
      return false;
    }
    if (stack.getItem() instanceof com.faktocraft.common.interfaces.item.IElectricItem electricItem) {
      if (electricItem.getEnergyTier().getLvl() > extractor.energy().energyTier().getLvl()) {
        return false;
      }
      if (electricItem.getEnergyType() == com.faktocraft.common.enums.EnergyType.RECEIVE) {
        return false;
      }
    }
    if (dockSlot == PipeExtractor.DOCK_TENSION_SLOT) {
      return stack.getItem() instanceof com.faktocraft.common.item.impl.upgrade.TensionUpgrade;
    }
    if (dockSlot == PipeExtractor.DOCK_BATTERY_SLOT) {
      return stack.is(com.faktocraft.common.registries.ModTags.BATTERIES);
    }
    return stack.getItem() instanceof com.faktocraft.common.item.impl.CapacitorItem;
  }

  private int dockTarget(ItemStack stack) {
    if (mayDock(PipeExtractor.DOCK_TENSION_SLOT, stack)) {
      return PipeExtractor.DOCK_TENSION_SLOT;
    }
    if (mayDock(PipeExtractor.DOCK_BATTERY_SLOT, stack)) {
      return PipeExtractor.DOCK_BATTERY_SLOT;
    }
    return mayDock(0, stack) ? 0 : -1;
  }

  public int getRunMode() {
    return runMode.get();
  }

  public PipeExtractor getExtractor() {
    return extractor;
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (extractor != null && id >= 0 && id <= 2) {
      extractor.setRunMode(id);
      return true;
    }
    return false;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (extractor == null) {
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
    } else if (PipeExtractor.isMotorUpgrade(stack)) {
      if (!moveItemStackTo(stack, 0, PipeExtractor.UPGRADE_SLOTS, false)) {
        return ItemStack.EMPTY;
      }
    } else if (dockTarget(stack) >= 0) {
      int target = DOCK_INDEX + dockTarget(stack);
      if (!moveItemStackTo(stack, target, target + 1, false)
          && !(dockTarget(stack) == 0
              && moveItemStackTo(stack, DOCK_INDEX + 1, DOCK_INDEX + 2, false))) {
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
    return blockEntity != null && !blockEntity.isRemoved()
        && player.distanceToSqr(blockEntity.getBlockPos().getCenter()) <= 64.0;
  }
}
