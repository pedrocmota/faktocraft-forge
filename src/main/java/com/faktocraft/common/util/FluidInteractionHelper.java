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
import java.util.ArrayList;
import java.util.List;

public final class FluidInteractionHelper {

  private FluidInteractionHelper() {
  }

  public static boolean isContainer(ItemStack stack) {
    return !stack.isEmpty() && !(stack.getItem() instanceof FluidItem)
        && FluidUtil.getFluidHandler(stack).isPresent();
  }

  public static boolean canPourContainer(ItemStack stack, FluidStorage tank) {
    return isContainer(stack) && !tank.isOutputOnly()
        && FluidUtil.tryEmptyContainer(stack, tank, Integer.MAX_VALUE, null, false).isSuccess();
  }

  public static boolean canFillContainer(ItemStack stack, FluidStorage tank) {
    return isContainer(stack)
        && FluidUtil.tryFillContainer(stack, tank, Integer.MAX_VALUE, null, false).isSuccess();
  }

  public static boolean pourCarried(Player player, FluidStorage tank, boolean all) {
    return transferCarried(player, tank, all, true);
  }

  public static boolean fillCarried(Player player, FluidStorage tank, boolean all) {
    return transferCarried(player, tank, all, false);
  }

  private static boolean transferCarried(Player player, FluidStorage tank, boolean all, boolean pour) {
    ItemStack carried = player.containerMenu.getCarried();
    if (pour ? !canPourContainer(carried, tank) : !canFillContainer(carried, tank)) {
      return false;
    }
    int limit = all ? carried.getCount() : 1;
    List<ItemStack> results = new ArrayList<>();
    while (results.size() < limit) {
      Player sound = results.isEmpty() ? player : null;
      FluidActionResult result = pour
          ? FluidUtil.tryEmptyContainer(carried, tank, Integer.MAX_VALUE, sound, true)
          : FluidUtil.tryFillContainer(carried, tank, Integer.MAX_VALUE, sound, true);
      if (!result.isSuccess()) {
        break;
      }
      results.add(result.getResult());
    }
    if (results.isEmpty()) {
      return false;
    }
    ItemStack first = results.get(0);
    if (carried.getCount() == results.size() && first.getMaxStackSize() >= results.size()) {
      player.containerMenu.setCarried(first.copyWithCount(results.size()));
      return true;
    }
    carried.shrink(results.size());
    if (carried.isEmpty()) {
      player.containerMenu.setCarried(ItemStack.EMPTY);
    }
    for (ItemStack result : results) {
      player.getInventory().placeItemBackInInventory(result);
    }
    return true;
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
