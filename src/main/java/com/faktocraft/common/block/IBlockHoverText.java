package com.faktocraft.common.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public interface IBlockHoverText {
  void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag);
}
