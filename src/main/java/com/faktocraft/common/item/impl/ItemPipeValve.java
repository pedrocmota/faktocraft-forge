package com.faktocraft.common.item.impl;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemPipeValve extends Item {

  public ItemPipeValve(Properties properties) {
    super(properties);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".pipe_valve")
        .withStyle(ChatFormatting.GRAY));
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".pipe_valve_usage")
        .withStyle(ChatFormatting.DARK_GRAY));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}
