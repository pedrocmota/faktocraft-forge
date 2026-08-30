package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.IndReb;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ThroughputUpgradeItem extends Item {

  public static final int PACK = 16;

  public ThroughputUpgradeItem(Properties properties) {
    super(properties.stacksTo(PACK));
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, tooltip, flag);
    tooltip.add(Component.translatable("logistics." + IndReb.MODID + ".throughput.desc")
        .withStyle(ChatFormatting.GRAY));
  }
}
