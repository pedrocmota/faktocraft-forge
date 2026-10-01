package com.faktocraft.common.block.impl.logistics;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ThroughputUpgradeItem extends Item {

  public static final int PACK = 16;

  public ThroughputUpgradeItem(Properties properties) {
    super(properties.stacksTo(PACK));
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, display, tooltip, flag);
    tooltip.accept(Component.translatable("logistics." + Faktocraft.MODID + ".throughput.desc")
        .withStyle(ChatFormatting.GRAY));
  }
}
