package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.util.FluidStackCompat;
import com.faktocraft.common.entity.block.FluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import com.faktocraft.common.util.transfer.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class TankColumnStorage implements IFluidHandler {

  private static final int MAX_COLUMN = 64;

  private final BlockEntityTank anchor;
  private long partsTick = Long.MIN_VALUE;
  private List<BlockEntityTank> partsCache = List.of();

  public TankColumnStorage(BlockEntityTank anchor) {
    this.anchor = anchor;
  }

  public List<FluidStorage> parts() {
    Level level = anchor.getLevel();
    if (level == null) {
      return List.of(anchor.tank);
    }
    long now = level.getGameTime();
    if (now != partsTick || partsCache.isEmpty()) {
      partsTick = now;
      partsCache = collectParts(level);
    }
    List<FluidStorage> list = new ArrayList<>(partsCache.size());
    for (BlockEntityTank tankEntity : partsCache) {
      if (!tankEntity.isRemoved()) {
        list.add(tankEntity.tank);
      }
    }
    return list.isEmpty() ? List.of(anchor.tank) : list;
  }

  private List<BlockEntityTank> collectParts(Level level) {
    FluidStack known = anchor.tank.isEmpty() ? FluidStack.EMPTY : anchor.tank.getFluidStack();
    BlockPos base = anchor.getBlockPos();
    int guard = 0;
    while (guard++ < MAX_COLUMN && level.getBlockEntity(base.below()) instanceof BlockEntityTank below) {
      if (!below.tank.isEmpty()) {
        if (!known.isEmpty() && !FluidStackCompat.isFluidEqual(known, below.tank.getFluidStack())) {
          break;
        }
        known = below.tank.getFluidStack();
      }
      base = base.below();
    }
    List<BlockEntityTank> list = new ArrayList<>();
    FluidStack columnFluid = FluidStack.EMPTY;
    BlockPos pos = base;
    guard = 0;
    while (guard++ < MAX_COLUMN && level.getBlockEntity(pos) instanceof BlockEntityTank tankEntity) {
      if (!tankEntity.tank.isEmpty()) {
        if (!columnFluid.isEmpty() && !FluidStackCompat.isFluidEqual(columnFluid, tankEntity.tank.getFluidStack())) {
          break;
        }
        columnFluid = tankEntity.tank.getFluidStack();
      }
      list.add(tankEntity);
      pos = pos.above();
    }
    return list.isEmpty() ? List.of(anchor) : list;
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
    return current.isEmpty() ? FluidStack.EMPTY : current.copyWithAmount(totalMb());
  }

  @Override
  public int getTankCapacity(int tankIndex) {
    return capacityMb();
  }

  @Override
  public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
    FluidStack current = variant();
    return current.isEmpty() || FluidStackCompat.isFluidEqual(current, stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    FluidStack current = variant();
    if (!current.isEmpty() && !FluidStackCompat.isFluidEqual(current, resource)) {
      return 0;
    }
    int filled = 0;
    for (FluidStorage part : parts()) {
      if (filled >= resource.getAmount()) {
        break;
      }
      filled += part.fill(resource.copyWithAmount(resource.getAmount() - filled), action);
    }
    return filled;
  }

  @Override
  public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
    FluidStack current = variant();
    if (resource.isEmpty() || current.isEmpty() || !FluidStackCompat.isFluidEqual(current, resource)) {
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
