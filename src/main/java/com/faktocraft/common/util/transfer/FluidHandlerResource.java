package com.faktocraft.common.util.transfer;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.ArrayList;
import java.util.List;

public class FluidHandlerResource extends SnapshotJournal<List<FluidStack>>
    implements ResourceHandler<FluidResource> {
  private final IFluidHandler handler;

  public FluidHandlerResource(IFluidHandler handler) {
    this.handler = handler;
  }

  public IFluidHandler handler() {
    return handler;
  }

  @Override
  protected List<FluidStack> createSnapshot() {
    List<FluidStack> copy = new ArrayList<>(handler.getTanks());
    for (int i = 0; i < handler.getTanks(); i++) {
      copy.add(handler.getFluidInTank(i).copy());
    }
    return copy;
  }

  @Override
  protected void revertToSnapshot(List<FluidStack> snapshot) {
    for (int i = 0; i < snapshot.size() && i < handler.getTanks(); i++) {
      handler.setFluidInTank(i, snapshot.get(i));
    }
  }

  @Override
  public int size() {
    return handler.getTanks();
  }

  @Override
  public FluidResource getResource(int index) {
    return FluidResource.of(handler.getFluidInTank(index));
  }

  @Override
  public long getAmountAsLong(int index) {
    return handler.getFluidInTank(index).getAmount();
  }

  @Override
  public long getCapacityAsLong(int index, FluidResource resource) {
    return handler.getTankCapacity(index);
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    return resource.isEmpty() || handler.isFluidValid(index, resource.toStack(1));
  }

  @Override
  public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0) {
      return 0;
    }
    FluidStack current = handler.getFluidInTank(index);
    if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource.toStack(1))) {
      return 0;
    }
    if (!handler.isFluidValid(index, resource.toStack(amount))) {
      return 0;
    }
    int simulated = handler.fill(resource.toStack(amount), IFluidHandler.FluidAction.SIMULATE);
    if (simulated <= 0) {
      return 0;
    }
    updateSnapshots(transaction);
    return handler.fill(resource.toStack(simulated), IFluidHandler.FluidAction.EXECUTE);
  }

  @Override
  public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0) {
      return 0;
    }
    FluidStack current = handler.getFluidInTank(index);
    if (current.isEmpty() || !FluidStack.isSameFluidSameComponents(current, resource.toStack(1))) {
      return 0;
    }
    FluidStack simulated = handler.drain(resource.toStack(Math.min(amount, current.getAmount())),
        IFluidHandler.FluidAction.SIMULATE);
    if (simulated.isEmpty()) {
      return 0;
    }
    updateSnapshots(transaction);
    return handler.drain(resource.toStack(simulated.getAmount()), IFluidHandler.FluidAction.EXECUTE).getAmount();
  }
}
