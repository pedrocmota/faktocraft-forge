package com.faktocraft.common.item.impl.upgrade;

import com.faktocraft.IndReb;
import com.faktocraft.common.enums.UpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

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
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip." + IndReb.MODID + ".tension_upgrade", mkLevel)
        .withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
