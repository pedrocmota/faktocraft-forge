package com.faktocraft.common.util.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.ArrayList;
import java.util.List;

public class ItemHandlerResource extends SnapshotJournal<List<ItemStack>> implements ResourceHandler<ItemResource> {
  private final IItemHandler handler;

  public ItemHandlerResource(IItemHandler handler) {
    this.handler = handler;
  }

  public IItemHandler handler() {
    return handler;
  }

  @Override
  protected List<ItemStack> createSnapshot() {
    List<ItemStack> copy = new ArrayList<>(handler.getSlots());
    for (int i = 0; i < handler.getSlots(); i++) {
      copy.add(handler.getStackInSlot(i).copy());
    }
    return copy;
  }

  @Override
  protected void revertToSnapshot(List<ItemStack> snapshot) {
    if (!handler.supportsSetStackInSlot()) {
      return;
    }
    for (int i = 0; i < snapshot.size() && i < handler.getSlots(); i++) {
      handler.setStackInSlot(i, snapshot.get(i));
    }
  }

  @Override
  public int size() {
    return handler.getSlots();
  }

  @Override
  public ItemResource getResource(int index) {
    return ItemResource.of(handler.getStackInSlot(index));
  }

  @Override
  public long getAmountAsLong(int index) {
    return handler.getStackInSlot(index).getCount();
  }

  @Override
  public long getCapacityAsLong(int index, ItemResource resource) {
    int limit = handler.getSlotLimit(index);
    return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
  }

  @Override
  public boolean isValid(int index, ItemResource resource) {
    return resource.isEmpty() || handler.isItemValid(index, resource.toStack());
  }

  @Override
  public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0) {
      return 0;
    }
    ItemStack remainder = handler.insertItem(index, resource.toStack(amount), true);
    int inserted = amount - remainder.getCount();
    if (inserted <= 0) {
      return 0;
    }
    updateSnapshots(transaction);
    handler.insertItem(index, resource.toStack(inserted), false);
    return inserted;
  }

  @Override
  public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0) {
      return 0;
    }
    ItemStack inSlot = handler.getStackInSlot(index);
    if (inSlot.isEmpty() || !resource.matches(inSlot)) {
      return 0;
    }
    ItemStack simulated = handler.extractItem(index, amount, true);
    if (simulated.isEmpty()) {
      return 0;
    }
    updateSnapshots(transaction);
    return handler.extractItem(index, simulated.getCount(), false).getCount();
  }
}
