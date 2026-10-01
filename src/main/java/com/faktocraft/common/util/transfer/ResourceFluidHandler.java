package com.faktocraft.common.util.transfer;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class ResourceFluidHandler implements IFluidHandler {
  private final ResourceHandler<FluidResource> handler;

  public ResourceFluidHandler(ResourceHandler<FluidResource> handler) {
    this.handler = handler;
  }

  public ResourceHandler<FluidResource> handler() {
    return handler;
  }

  @Override
  public int getTanks() {
    return handler.size();
  }

  @NotNull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return FluidUtil.getStack(handler, tank);
  }

  @Override
  public int getTankCapacity(int tank) {
    return handler.getCapacityAsInt(tank, handler.getResource(tank));
  }

  @Override
  public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
    return handler.isValid(tank, FluidResource.of(stack));
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource == null || resource.isEmpty()) {
      return 0;
    }
    try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      int inserted = handler.insert(FluidResource.of(resource), resource.getAmount(), tx);
      if (action.execute()) {
        tx.commit();
      }
      return inserted;
    }
  }

  @NotNull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource == null || resource.isEmpty()) {
      return FluidStack.EMPTY;
    }
    try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
      int extracted = handler.extract(FluidResource.of(resource), resource.getAmount(), tx);
      if (action.execute()) {
        tx.commit();
      }
      return extracted <= 0 ? FluidStack.EMPTY : resource.copyWithAmount(extracted);
    }
  }

  @NotNull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    if (maxDrain <= 0) {
      return FluidStack.EMPTY;
    }
    for (int i = 0; i < handler.size(); i++) {
      FluidResource resource = handler.getResource(i);
      if (resource.isEmpty()) {
        continue;
      }
      try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
        int extracted = handler.extract(resource, maxDrain, tx);
        if (action.execute()) {
          tx.commit();
        }
        if (extracted > 0) {
          return resource.toStack(extracted);
        }
      }
    }
    return FluidStack.EMPTY;
  }
}
