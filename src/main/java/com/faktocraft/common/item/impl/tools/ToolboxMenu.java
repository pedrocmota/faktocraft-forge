package com.faktocraft.common.item.impl.tools;

import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ToolboxMenu extends AbstractContainerMenu {

  public static final int SIZE = 8;
  private static final String TAG_ITEMS = "Items";

  private final ItemStack toolboxStack;
  private final ItemStackHandler handler;

  public ToolboxMenu(int windowId, Inventory playerInventory, BlockPos ignoredPos) {
    super(M1Registry.TOOLBOX_MENU, windowId);
    Player player = playerInventory.player;
    ItemStack main = player.getMainHandItem();
    this.toolboxStack = main.getItem() instanceof ToolboxItem ? main : player.getOffhandItem();

    this.handler = new ItemStackHandler(SIZE) {
      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        return ToolboxItem.isTool(stack);
      }

      @Override
      protected void onContentsChanged(int slot) {
        toolboxStack.getOrCreateTag().put(TAG_ITEMS, serializeNBT());
      }
    };
    if (toolboxStack.hasTag() && toolboxStack.getTag().contains(TAG_ITEMS)) {
      handler.deserializeNBT(toolboxStack.getTag().getCompound(TAG_ITEMS));
    }

    for (int i = 0; i < SIZE; i++) {
      addSlot(new SlotItemHandler(handler, i, 17 + i * 18, 24));
    }
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        addSlot(new Slot(playerInventory, 9 + row * 9 + col, 8 + col * 18, 58 + row * 18));
      }
    }
    for (int col = 0; col < 9; col++) {
      addSlot(new Slot(playerInventory, col, 8 + col * 18, 116));
    }
  }

  @Override
  public void clicked(int slotId, int button, ClickType clickType, Player player) {
    if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).getItem() == toolboxStack) {
      return;
    }
    if (clickType == ClickType.SWAP) {
      ItemStack swap = button == 40 ? player.getInventory().offhand.get(0)
          : player.getInventory().getItem(button);
      if (swap == toolboxStack) {
        return;
      }
    }
    super.clicked(slotId, button, clickType, player);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    Slot slot = slots.get(index);
    if (!slot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack moved = slot.getItem();
    ItemStack original = moved.copy();
    if (index < SIZE) {
      if (!moveItemStackTo(moved, SIZE, slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else {
      if (!ToolboxItem.isTool(moved) || !moveItemStackTo(moved, 0, SIZE, false)) {
        return ItemStack.EMPTY;
      }
    }
    if (moved.isEmpty()) {
      slot.set(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }
    return original;
  }

  @Override
  public boolean stillValid(Player player) {
    return player.getMainHandItem() == toolboxStack || player.getOffhandItem() == toolboxStack;
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    if (!player.level().isClientSide()) {
      player.level().playSound(null, player.blockPosition(),
          net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE,
          net.minecraft.sounds.SoundSource.PLAYERS, 0.4F, 1.3F);
    }
  }
}
