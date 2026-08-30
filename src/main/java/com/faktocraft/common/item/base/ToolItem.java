package com.faktocraft.common.item.base;

import net.minecraft.world.item.ItemStack;

public class ToolItem extends BaseItem {

  public ToolItem(Properties properties, int maxDamage) {
    super(properties.stacksTo(1).durability(maxDamage));
  }

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  @Override
  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return stack.getDamageValue() + 1 < stack.getMaxDamage();
  }

  @Override
  public ItemStack getCraftingRemainingItem(ItemStack stack) {
    ItemStack result = stack.copy();
    result.setDamageValue(stack.getDamageValue() + 1);
    return result.getDamageValue() >= result.getMaxDamage() ? ItemStack.EMPTY : result;
  }
}
