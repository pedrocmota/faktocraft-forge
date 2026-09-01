package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

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
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, tooltip, flag);
    tooltip.add(Component.translatable("logistics." + Faktocraft.MODID + ".module." + type.id() + ".desc")
        .withStyle(ChatFormatting.GRAY));
    if (ModuleSettings.hasTree(stack)) {
      if (!ModuleSettings.treeOverrides(stack).isEmpty()) {
        tooltip.add(Component.translatable("logistics." + Faktocraft.MODID + ".module.tree_configured")
            .withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      int lines = ModuleSettings.lineCount(stack);
      if (lines > 0) {
        tooltip.add(Component.translatable("logistics." + Faktocraft.MODID + ".module.configured", lines)
            .withStyle(ChatFormatting.DARK_GRAY));
      }
    }
  }
}
