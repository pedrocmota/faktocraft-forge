package com.faktocraft.common.item.impl.reactor;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FuelRodItem extends ReactorComponentItem {

  public FuelRodItem(Properties properties) {
    super(properties.durability(BlockEntityNuclearReactor.ROD_DAMAGE_STEPS), "fuel_rod");
  }

  public static int lifePercent(ItemStack stack) {
    int max = Math.max(1, stack.getMaxDamage());
    return Math.round((max - stack.getDamageValue()) * 100.0F / max);
  }

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  @Override
  public boolean isRepairable(ItemStack stack) {
    return false;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, tooltip, flag);
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".fuel_rod_life", lifePercent(stack))
        .withStyle(ChatFormatting.DARK_GREEN));
  }
}
