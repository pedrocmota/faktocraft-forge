package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.entity.block.FluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class TankColumnStorage implements IFluidHandler {

  private static final int MAX_COLUMN = 64;

  private final BlockEntityTank anchor;

  public TankColumnStorage(BlockEntityTank anchor) {
    this.anchor = anchor;
  }

  public List<FluidStorage> parts() {
    Level level = anchor.getLevel();
    if (level == null) {
      return List.of(anchor.tank);
    }
    FluidStack known = anchor.tank.isEmpty() ? FluidStack.EMPTY : anchor.tank.getFluidStack();
    BlockPos base = anchor.getBlockPos();
    int guard = 0;
    while (guard++ < MAX_COLUMN && level.getBlockEntity(base.below()) instanceof BlockEntityTank below) {
      if (!below.tank.isEmpty()) {
        if (!known.isEmpty() && !known.isFluidEqual(below.tank.getFluidStack())) {
          break;
        }
        known = below.tank.getFluidStack();
      }
      base = base.below();
    }
    List<FluidStorage> list = new ArrayList<>();
    FluidStack columnFluid = FluidStack.EMPTY;
    BlockPos pos = base;
    guard = 0;
    while (guard++ < MAX_COLUMN && level.getBlockEntity(pos) instanceof BlockEntityTank tankEntity) {
      if (!tankEntity.tank.isEmpty()) {
        if (!columnFluid.isEmpty() && !columnFluid.isFluidEqual(tankEntity.tank.getFluidStack())) {
          break;
        }
        columnFluid = tankEntity.tank.getFluidStack();
      }
      list.add(tankEntity.tank);
      pos = pos.above();
    }
    return list.isEmpty() ? List.of(anchor.tank) : list;
  }

  public FluidStack variant() {
    for (FluidStorage part : parts()) {
      if (!part.isEmpty()) {
        return part.getFluidStack();
      }
    }
    return FluidStack.EMPTY;
  }

  public int totalMb() {
    int total = 0;
    for (FluidStorage part : parts()) {
      total += part.getFluidAmount();
    }
    return total;
  }

  public int capacityMb() {
    return parts().size() * BlockEntityTank.CAPACITY_MB;
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public @NotNull FluidStack getFluidInTank(int tankIndex) {
    FluidStack current = variant();
    return current.isEmpty() ? FluidStack.EMPTY : new FluidStack(current, totalMb());
  }

  @Override
  public int getTankCapacity(int tankIndex) {
    return capacityMb();
  }

  @Override
  public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
    FluidStack current = variant();
    return current.isEmpty() || current.isFluidEqual(stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    FluidStack current = variant();
    if (!current.isEmpty() && !current.isFluidEqual(resource)) {
      return 0;
    }
    int filled = 0;
    for (FluidStorage part : parts()) {
      if (filled >= resource.getAmount()) {
        break;
      }
      filled += part.fill(new FluidStack(resource, resource.getAmount() - filled), action);
    }
    return filled;
  }

  @Override
  public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
    FluidStack current = variant();
    if (resource.isEmpty() || current.isEmpty() || !current.isFluidEqual(resource)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @Override
  public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
    FluidStack current = variant();
    if (current.isEmpty() || maxDrain <= 0) {
      return FluidStack.EMPTY;
    }
    FluidStack result = current.copy();
    int drained = 0;
    List<FluidStorage> parts = parts();
    for (int i = parts.size() - 1; i >= 0; i--) {
      if (drained >= maxDrain) {
        break;
      }
      FluidStack taken = parts.get(i).drain(maxDrain - drained, action);
      drained += taken.getAmount();
    }
    if (drained <= 0) {
      return FluidStack.EMPTY;
    }
    result.setAmount(drained);
    return result;
  }
}
