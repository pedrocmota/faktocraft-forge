package com.faktocraft.common.item.impl.tools;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemWindRotor extends Item {

  private final double generationFactor;

  public ItemWindRotor(Properties properties, double generationFactor) {
    super(properties.stacksTo(1));
    this.generationFactor = generationFactor;
  }

  public static double factorOf(ItemStack stack) {
    return stack.getItem() instanceof ItemWindRotor rotor ? rotor.generationFactor : 0.0;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".wind_rotor_desc")
        .withStyle(ChatFormatting.GRAY));
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".wind_rotor_boost",
        (int) Math.round(generationFactor * 100.0)).withStyle(ChatFormatting.DARK_AQUA));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}
