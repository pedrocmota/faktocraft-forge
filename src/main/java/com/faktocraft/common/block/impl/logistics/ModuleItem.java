package com.faktocraft.common.block.impl.logistics;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class ModuleItem extends Item {

  public static final int PACK = 16;

  private final ModuleType type;

  public ModuleItem(ModuleType type, Properties properties) {
    super(properties.stacksTo(PACK));
    this.type = type;
  }

  public ModuleType getType() {
    return type;
  }

  @Nullable
  public static ModuleType typeOf(ItemStack stack) {
    return stack.getItem() instanceof ModuleItem module ? module.getType() : null;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, display, tooltip, flag);
    tooltip.accept(Component.translatable("logistics." + Faktocraft.MODID + ".module." + type.id() + ".desc")
        .withStyle(ChatFormatting.GRAY));
    if (ModuleSettings.hasTree(stack)) {
      if (!ModuleSettings.treeOverrides(stack).isEmpty()) {
        tooltip.accept(Component.translatable("logistics." + Faktocraft.MODID + ".module.tree_configured")
            .withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      int lines = ModuleSettings.lineCount(stack);
      if (lines > 0) {
        tooltip.accept(Component.translatable("logistics." + Faktocraft.MODID + ".module.configured", lines)
            .withStyle(ChatFormatting.DARK_GRAY));
      }
    }
  }
}
