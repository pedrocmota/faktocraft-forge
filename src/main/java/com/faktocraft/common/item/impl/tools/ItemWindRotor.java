package com.faktocraft.common.item.impl.tools;

import com.faktocraft.IndReb;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

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
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip." + IndReb.MODID + ".wind_rotor_desc")
        .withStyle(ChatFormatting.GRAY));
    tooltip.add(Component.translatable("tooltip." + IndReb.MODID + ".wind_rotor_boost",
        (int) Math.round(generationFactor * 100.0)).withStyle(ChatFormatting.DARK_AQUA));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
