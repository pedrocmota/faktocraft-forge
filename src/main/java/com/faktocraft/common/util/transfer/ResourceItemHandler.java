package com.faktocraft.common.util.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class ResourceItemHandler implements IItemHandler {
  private final ResourceHandler<ItemResource> handler;

  public ResourceItemHandler(ResourceHandler<ItemResource> handler) {
    this.handler = handler;
  }

  public ResourceHandler<ItemResource> handler() {
    return handler;
  }

  @Override
  public int getSlots() {
    return handler.size();
  }

  @NotNull
  @Override
  public ItemStack getStackInSlot(int slot) {
    return ItemUtil.getStack(handler, slot);
  }

  @NotNull
  @Override
  public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
      if (!simulate) {
        tx.commit();
      }
      int leftover = stack.getCount() - inserted;
      return leftover <= 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
    }
  }

  @NotNull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount <= 0) {
      return ItemStack.EMPTY;
    }
    ItemResource resource = handler.getResource(slot);
    if (resource.isEmpty()) {
      return ItemStack.EMPTY;
    }
    try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      int extracted = handler.extract(slot, resource, Math.min(amount, resource.getMaxStackSize()), tx);
      if (!simulate) {
        tx.commit();
      }
      return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
    }
  }

  @Override
  public int getSlotLimit(int slot) {
    ItemResource resource = handler.getResource(slot);
    return handler.getCapacityAsInt(slot, resource);
  }

  @Override
  public boolean isItemValid(int slot, @NotNull ItemStack stack) {
    return handler.isValid(slot, ItemResource.of(stack));
  }
}
