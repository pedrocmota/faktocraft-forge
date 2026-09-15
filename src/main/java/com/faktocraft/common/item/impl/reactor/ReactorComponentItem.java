package com.faktocraft.common.item.impl.reactor;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.BaseItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ReactorComponentItem extends BaseItem {

  private final String tooltipKey;

  public ReactorComponentItem(Properties properties, String name) {
    super(properties);
    this.tooltipKey = "tooltip." + Faktocraft.MODID + "." + name;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
