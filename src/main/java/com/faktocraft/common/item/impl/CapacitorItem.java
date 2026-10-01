package com.faktocraft.common.item.impl;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class CapacitorItem extends BaseItem {

  private final int capacity;

  public CapacitorItem(Properties properties, int capacity) {
    super(properties);
    this.capacity = capacity;
  }

  public int getCapacity() {
    return capacity;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".capacitor_capacity",
        TextComponentUtil.getFormattedEnergyUnit(capacity)).withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}
