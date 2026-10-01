package com.faktocraft.common.item.impl.upgrade;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.UpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TensionUpgrade extends ItemUpgrade {

  private final int mkLevel;

  public TensionUpgrade(Properties properties, int mkLevel) {
    super(properties, UpgradeType.TRANSFORMER);
    this.mkLevel = mkLevel;
  }

  public int getMkLevel() {
    return mkLevel;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".tension_upgrade", mkLevel)
        .withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}
