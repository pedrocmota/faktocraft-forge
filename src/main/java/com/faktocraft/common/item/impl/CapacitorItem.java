package com.faktocraft.common.item.impl;

import com.faktocraft.IndReb;
import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

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
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip." + IndReb.MODID + ".capacitor_capacity",
        TextComponentUtil.getFormattedEnergyUnit(capacity)).withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
