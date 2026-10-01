package com.faktocraft.common.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public final class ItemStackUtil {
  private ItemStackUtil() {
  }

  public static ItemStack craftingRemainder(ItemStack stack) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStackTemplate template = stack.getItem().getCraftingRemainder(stack);
    return template == null ? ItemStack.EMPTY : template.create();
  }
}
