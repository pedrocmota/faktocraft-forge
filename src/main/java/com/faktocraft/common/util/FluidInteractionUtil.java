package com.faktocraft.common.util;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.item.base.FluidItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public final class FluidInteractionUtil {

  private FluidInteractionUtil() {
  }

  public static Fluid getContainedFluid(ItemStack stack) {
    if (stack.getItem() instanceof BucketItem bucketItem) {
      return bucketItem.getFluid();
    }
    if (stack.getItem() instanceof FluidItem) {
      return FluidItem.getFluid(stack);
    }
    return Fluids.EMPTY;
  }

  public static int getContainedAmount(ItemStack stack) {
    if (stack.getItem() instanceof BucketItem bucketItem) {
      return bucketItem.getFluid() == Fluids.EMPTY ? 0 : 1000;
    }
    if (stack.getItem() instanceof FluidItem) {
      return FluidItem.getFluidAmount(stack);
    }
    return 0;
  }

  public static boolean fillTankFromHeld(Player player, ItemStack heldStack, FluidStorage tank,
      @Nullable Fluid requiredFluid) {
    if (heldStack.isEmpty()) {
      return false;
    }

    Fluid fluid = getContainedFluid(heldStack);
    int amount = getContainedAmount(heldStack);
    if (fluid == Fluids.EMPTY || amount <= 0) {
      return false;
    }
    if (requiredFluid != null && fluid != requiredFluid) {
      return false;
    }

    FluidStack fluidStack = new FluidStack(fluid, amount);
    if (tank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) != amount) {
      return false;
    }

    ItemStack emptyContainer;
    if (heldStack.getItem() instanceof BucketItem) {
      emptyContainer = new ItemStack(Items.BUCKET);
    } else {
      emptyContainer = heldStack.copyWithCount(1);
      FluidItem.setFluid(emptyContainer, Fluids.EMPTY, 0);
    }

    tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
    heldStack.shrink(1);
    if (!player.addItem(emptyContainer)) {
      player.drop(emptyContainer, false);
    }
    return true;
  }
}
