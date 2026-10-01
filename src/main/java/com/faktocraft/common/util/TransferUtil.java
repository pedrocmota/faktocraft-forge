package com.faktocraft.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.faktocraft.common.util.transfer.CapabilityBridge;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.IFluidHandler;
import com.faktocraft.common.util.transfer.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class TransferUtil {

  private TransferUtil() {
  }

  @Nullable
  public static IItemHandler findItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
    BlockEntity be = level.getBlockEntity(pos);
    if (be == null) {
      return null;
    }
    return CapabilityBridge.get(be, ForgeCapabilities.ITEM_HANDLER, side);
  }

  @Nullable
  public static IFluidHandler findFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
    BlockEntity be = level.getBlockEntity(pos);
    if (be == null) {
      return null;
    }
    return CapabilityBridge.get(be, ForgeCapabilities.FLUID_HANDLER, side);
  }

  public static int moveFluid(IFluidHandler from, IFluidHandler to, int maxMb) {
    FluidStack drained = from.drain(maxMb, IFluidHandler.FluidAction.SIMULATE);
    if (drained.isEmpty()) {
      return 0;
    }
    int fillable = to.fill(drained, IFluidHandler.FluidAction.SIMULATE);
    if (fillable <= 0) {
      return 0;
    }

    FluidStack moved = from.drain(drained.copyWithAmount(fillable), IFluidHandler.FluidAction.EXECUTE);
    if (moved.isEmpty()) {
      return 0;
    }
    int filled = to.fill(moved, IFluidHandler.FluidAction.EXECUTE);

    if (filled < moved.getAmount()) {
      from.fill(moved.copyWithAmount(moved.getAmount() - filled), IFluidHandler.FluidAction.EXECUTE);
    }
    return filled;
  }
}
