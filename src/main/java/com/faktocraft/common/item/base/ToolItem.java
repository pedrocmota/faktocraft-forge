package com.faktocraft.common.item.base;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

public class ToolItem extends BaseItem {
  public ToolItem(Properties properties, int maxDamage) {
    super(properties.stacksTo(1).durability(maxDamage));
  }

  @Nullable
  @Override
  public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
    if (!(instance instanceof ItemStack stack) || stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
      return null;
    }
    ItemStack result = stack.copy();
    result.setDamageValue(stack.getDamageValue() + 1);
    return result.getDamageValue() >= result.getMaxDamage() ? null
        : new ItemStackTemplate(result.typeHolder(), result.getCount(), result.getComponentsPatch());
  }
}
