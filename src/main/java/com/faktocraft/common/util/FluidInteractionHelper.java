package com.faktocraft.common.util;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.item.base.FluidItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public final class FluidInteractionHelper {

  private FluidInteractionHelper() {
  }

  public static boolean tryFillTankFromHand(Player player, InteractionHand hand, FluidStorage tank, Fluid fluid) {
    ItemStack stack = player.getItemInHand(hand);
    if (stack.isEmpty()) {
      return false;
    }

    if (stack.getItem() instanceof FluidItem) {
      if (FluidItem.getFluid(stack) != fluid) {
        return false;
      }
      int amount = FluidItem.getFluidAmount(stack);
      if (amount <= 0 || tank.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.SIMULATE) != amount) {
        return false;
      }

      tank.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
      if (stack.getCount() == 1) {
        FluidItem.setFluid(stack, Fluids.EMPTY, 0);
      } else {
        ItemStack emptyCell = stack.copyWithCount(1);
        FluidItem.setFluid(emptyCell, Fluids.EMPTY, 0);
        stack.shrink(1);
        if (!player.addItem(emptyCell)) {
          player.drop(emptyCell, false);
        }
      }
      return true;
    }

    FluidStack contained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
    if (contained.isEmpty() || contained.getFluid() != fluid) {
      return false;
    }

    FluidActionResult result = FluidUtil.tryEmptyContainerAndStow(stack, tank,
        new InvWrapper(player.getInventory()), Integer.MAX_VALUE, player, true);
    if (result.isSuccess()) {
      player.setItemInHand(hand, result.getResult());
      return true;
    }
    return false;
  }
}
