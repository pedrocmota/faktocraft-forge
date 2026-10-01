package com.faktocraft.common.item.impl;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TeleportCardItem extends BaseItem {

  public TeleportCardItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    BlockPos target = ModComponents.getTeleportTarget(stack);

    if (target == null) {
      tooltip.accept(TextComponentUtil.build(
          Component.literal("< ").withStyle(ChatFormatting.GRAY),
          EnumLang.REPLICATION_EMPTY.getTranslationComponent(),
          Component.literal(" >").withStyle(ChatFormatting.GRAY)));
    } else {
      tooltip.accept(TextComponentUtil.build(
          Component.literal("< ").withStyle(ChatFormatting.GRAY),
          Component.literal(target.getX() + ", " + target.getY() + ", " + target.getZ())
              .withStyle(ChatFormatting.AQUA),
          Component.literal(" >").withStyle(ChatFormatting.GRAY)));
      net.minecraft.resources.ResourceKey<Level> dimension = ModComponents.getTeleportTargetDimension(stack, null);
      if (dimension != null) {
        tooltip.accept(Component.translatable("tooltip.faktocraft.teleport_card_dimension",
            com.faktocraft.common.block.impl.teleport_anchor.BlockTeleportAnchor.dimensionText(dimension))
            .withStyle(ChatFormatting.GRAY));
      }
    }

    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}
