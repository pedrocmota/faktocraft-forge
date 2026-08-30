package com.faktocraft.common.util;

import net.minecraft.world.item.ItemStack;

public class StackHandlerHelper {

  public static void shrinkInputStack(ItemStackHandler handler, int slotId, int shrinkCount) {
    ItemStack stack = handler.getStackInSlot(slotId).copy();
    stack.shrink(shrinkCount);
    handler.setStackInSlot(slotId, stack.isEmpty() ? ItemStack.EMPTY : stack);
  }

  public static void incMachineOutputStack(ItemStackHandler handler, int slotId, ItemStack resultStack) {
    ItemStack current = handler.getStackInSlot(slotId);
    if (current.isEmpty()) {
      handler.setStackInSlot(slotId, resultStack);
    } else {
      ItemStack grown = current.copy();
      grown.grow(resultStack.getCount());
      handler.setStackInSlot(slotId, grown);
    }
  }
}
