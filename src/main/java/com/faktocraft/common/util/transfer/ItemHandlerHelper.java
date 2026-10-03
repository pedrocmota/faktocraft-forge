package com.faktocraft.common.util.transfer;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class ItemHandlerHelper {

  private ItemHandlerHelper() {
  }

  @NotNull
  public static ItemStack insertItem(IItemHandler dest, @NotNull ItemStack stack, boolean simulate) {
    if (dest == null || stack.isEmpty()) {
      return stack;
    }
    for (int i = 0; i < dest.getSlots(); i++) {
      stack = dest.insertItem(i, stack, simulate);
      if (stack.isEmpty()) {
        return ItemStack.EMPTY;
      }
    }
    return stack;
  }

  public static boolean canItemStacksStack(@NotNull ItemStack a, @NotNull ItemStack b) {
    return !a.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
  }

  @NotNull
  public static ItemStack copyStackWithSize(@NotNull ItemStack stack, int size) {
    if (size == 0) {
      return ItemStack.EMPTY;
    }
    return stack.copyWithCount(size);
  }

  @NotNull
  public static ItemStack insertItemStacked(IItemHandler inventory, @NotNull ItemStack stack, boolean simulate) {
    if (inventory == null || stack.isEmpty()) {
      return stack;
    }
    if (!stack.isStackable()) {
      return insertItem(inventory, stack, simulate);
    }
    int sizeInventory = inventory.getSlots();
    for (int slot = 0; slot < sizeInventory; slot++) {
      ItemStack slotStack = inventory.getStackInSlot(slot);
      if (canItemStacksStack(stack, slotStack)) {
        stack = inventory.insertItem(slot, stack, simulate);
        if (stack.isEmpty()) {
          break;
        }
      }
    }
    if (!stack.isEmpty()) {
      for (int slot = 0; slot < sizeInventory; slot++) {
        if (inventory.getStackInSlot(slot).isEmpty()) {
          stack = inventory.insertItem(slot, stack, simulate);
          if (stack.isEmpty()) {
            break;
          }
        }
      }
    }
    return stack;
  }

  public static void giveItemToPlayer(Player player, @NotNull ItemStack stack) {
    giveItemToPlayer(player, stack, -1);
  }

  public static void giveItemToPlayer(Player player, @NotNull ItemStack stack, int preferredSlot) {
    if (stack.isEmpty()) {
      return;
    }
    ItemStack remainder = stack.copy();
    if (preferredSlot >= 0 && preferredSlot < player.getInventory().getContainerSize()) {
      ItemStack existing = player.getInventory().getItem(preferredSlot);
      if (existing.isEmpty()) {
        player.getInventory().setItem(preferredSlot, remainder);
        remainder = ItemStack.EMPTY;
      } else if (canItemStacksStack(existing, remainder)) {
        int room = Math.min(existing.getMaxStackSize(), player.getInventory().getMaxStackSize())
            - existing.getCount();
        int moved = Math.min(room, remainder.getCount());
        if (moved > 0) {
          existing.grow(moved);
          remainder.shrink(moved);
        }
      }
    }
    if (!remainder.isEmpty()) {
      player.getInventory().add(remainder);
    }
    Level level = player.level();
    if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
      float pitch = ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F;
      level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP,
          SoundSource.PLAYERS, 0.2F, pitch);
    }
    if (!remainder.isEmpty() && !level.isClientSide()) {
      ItemEntity entity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
      entity.setPickUpDelay(40);
      entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
      level.addFreshEntity(entity);
    }
    player.containerMenu.broadcastChanges();
  }
}
