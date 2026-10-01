package com.faktocraft.common.item.impl.reactor;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.BaseItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ReactorComponentItem extends BaseItem {

  private final String tooltipKey;

  public ReactorComponentItem(Properties properties, String name) {
    super(properties);
    this.tooltipKey = "tooltip." + Faktocraft.MODID + "." + name;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}
