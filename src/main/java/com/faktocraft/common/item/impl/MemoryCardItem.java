package com.faktocraft.common.item.impl;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.common.capabilities.scan_result.ScannerResult;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.registries.ModComponentsFluids;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class MemoryCardItem extends BaseItem {

  public MemoryCardItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static ScannerResult getScannerResult(ItemStack stack) {
    ScannerResult result = ModComponentsFluids.getScannerResult(stack);
    return result == null ? ScannerResult.EMPTY : result;
  }

  public static void setScannerResult(ItemStack stack, ScannerResult result) {
    if (result == null || result.isEmpty()) {
      ModComponentsFluids.removeScannerResult(stack);
    } else {
      ModComponentsFluids.setScannerResult(stack, result);
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    ScannerResult scannerResult = getScannerResult(stack);

    if (scannerResult.isEmpty()) {
      tooltip.accept(TextComponentUtil.build(
          Component.literal("< ").withStyle(ChatFormatting.GRAY),
          EnumLang.REPLICATION_EMPTY.getTranslationComponent(),
          Component.literal(" >").withStyle(ChatFormatting.GRAY)));
    } else {
      tooltip.accept(TextComponentUtil.build(
          Component.literal("< ").withStyle(ChatFormatting.GRAY),
          Component.literal(scannerResult.getResultStack().getHoverName().getString()),
          Component.literal(" >").withStyle(ChatFormatting.GRAY)));

      if (scannerResult.getMatterCost() > 0) {
        tooltip.accept(TextComponentUtil.build(
            EnumLang.MATTER_COST.getTranslationComponent().withStyle(ChatFormatting.DARK_GRAY),
            Component.literal(" " + scannerResult.getMatterCost() + " mB").withStyle(ChatFormatting.GRAY)));
      }

      if (scannerResult.getEnergyCost() > 0) {
        tooltip.accept(TextComponentUtil.build(
            EnumLang.ENERGY_COST.getTranslationComponent().withStyle(ChatFormatting.DARK_GRAY),
            Component.literal(" " + TextComponentUtil.getFormattedEnergyUnit(scannerResult.getEnergyCost()) + " IE/t")
                .withStyle(ChatFormatting.GRAY)));
      }
    }

    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}
