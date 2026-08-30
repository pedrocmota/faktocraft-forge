package com.faktocraft.common.item.impl;

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
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class TeleportCardItem extends BaseItem {

  public TeleportCardItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    BlockPos target = ModComponents.getTeleportTarget(stack);

    if (target == null) {
      tooltip.add(TextComponentUtil.build(
          Component.literal("< ").withStyle(ChatFormatting.GRAY),
          EnumLang.REPLICATION_EMPTY.getTranslationComponent(),
          Component.literal(" >").withStyle(ChatFormatting.GRAY)));
    } else {
      tooltip.add(TextComponentUtil.build(
          Component.literal("< ").withStyle(ChatFormatting.GRAY),
          Component.literal(target.getX() + ", " + target.getY() + ", " + target.getZ())
              .withStyle(ChatFormatting.AQUA),
          Component.literal(" >").withStyle(ChatFormatting.GRAY)));
    }

    super.appendHoverText(stack, level, tooltip, flag);
  }
}
