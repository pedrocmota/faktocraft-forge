package com.faktocraft.common.item.block;

import com.faktocraft.common.block.IBlockHoverText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import java.util.function.Consumer;

public class FaktocraftBlockItem extends BlockItem {
  public FaktocraftBlockItem(Block block, Item.Properties properties) {
    super(block, properties);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, display, tooltip, flag);
    if (getBlock() instanceof IBlockHoverText hook) {
      hook.appendHoverText(stack, context, display, tooltip, flag);
    }
  }
}
