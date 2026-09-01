package com.faktocraft.common.item.impl;

import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ItemPipeValve extends Item {

  public ItemPipeValve(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".pipe_valve")
        .withStyle(ChatFormatting.GRAY));
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".pipe_valve_usage")
        .withStyle(ChatFormatting.DARK_GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
