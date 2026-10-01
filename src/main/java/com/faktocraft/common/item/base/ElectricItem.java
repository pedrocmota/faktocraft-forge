package com.faktocraft.common.item.base;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.common.energy.impl.ItemEnergyWrapper;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.interfaces.item.IElectricItem;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ElectricItem extends BaseItem implements IElectricItem {

  protected final int initialEnergy;
  protected final int maxEnergy;
  protected final EnergyType energyType;
  protected final EnergyTier energyTier;

  public ElectricItem(Properties properties, int energyStored, int maxEnergy, EnergyType energyType,
      EnergyTier energyTier) {
    super(properties.stacksTo(1));
    this.initialEnergy = energyStored;
    this.maxEnergy = maxEnergy;
    this.energyType = energyType;
    this.energyTier = energyTier;
  }

  @Override
  public EnergyTier getEnergyTier() {
    return energyTier;
  }

  @Override
  public EnergyType getEnergyType() {
    return energyType;
  }

  public int getMaxEnergy() {
    return maxEnergy;
  }

  @Override
  public IEnergy getEnergy(ItemStack stack) {
    if (initialEnergy > 0 && !ModComponents.hasEnergy(stack)) {
      ModComponents.setEnergy(stack, initialEnergy);
    }
    return new ItemEnergyWrapper(stack, maxEnergy, energyType, energyTier);
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return Math.round(13.0F - ((1 - getChargeRatio(stack)) * 13.0F));
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return Mth.hsvToRgb(0, 1.0F, 1.0F);
  }

  public static float getChargeRatio(ItemStack stack) {
    if (stack.getItem() instanceof IElectricItem electricItem) {
      IEnergy energy = electricItem.getEnergy(stack);
      if (energy != null && energy.maxEnergy() > 0) {
        return (float) energy.energyStored() / energy.maxEnergy();
      }
    }
    return 0f;
  }

  public ItemStack makeFullStack() {
    ItemStack full = new ItemStack(this);
    ModComponents.setEnergy(full, maxEnergy);
    return full;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(EnumLang.POWER_TIER.getTranslationComponent(
        energyTier.getLang().getTranslationComponent().withStyle(energyTier.getColor()))
        .withStyle(ChatFormatting.GRAY)
        .append(Component.literal(" (" + TextComponentUtil.getFormattedLong(energyTier.getBasicTransfer()) + " IE/t)")
            .withStyle(ChatFormatting.DARK_GRAY)));

    int energyStored = Mth.clamp(ModComponents.getEnergy(stack, initialEnergy), 0, maxEnergy);
    tooltip.accept(EnumLang.STORED.getTranslationComponent(TextComponentUtil.build(
        EnumLang.POWER.getTranslationComponent(TextComponentUtil.getFormattedEnergyUnit(energyStored))
            .withStyle(energyTier.getColor()),
        Component.literal(" / ").withStyle(ChatFormatting.GRAY),
        EnumLang.POWER.getTranslationComponent(TextComponentUtil.getFormattedEnergyUnit(maxEnergy))
            .withStyle(energyTier.getColor())))
        .withStyle(ChatFormatting.GRAY));

    super.appendHoverText(stack, level, display, tooltip, flag);
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    return slotChanged || oldStack.getItem() != newStack.getItem();
  }
}
