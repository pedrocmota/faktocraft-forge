package com.faktocraft.common.item.base;

import com.faktocraft.common.energy.impl.ItemEnergyWrapper;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.interfaces.item.IElectricItem;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ElectricArmorItem extends ArmorItem implements IElectricItem {

  protected final int initialEnergy;
  protected final int maxEnergy;
  protected final EnergyType energyType;
  protected final EnergyTier energyTier;

  public ElectricArmorItem(ArmorMaterial material, ArmorItem.Type armorType, Properties properties,
      int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    super(material, armorType, properties.stacksTo(1));
    this.initialEnergy = energyStored;
    this.maxEnergy = maxEnergy;
    this.energyType = energyType;
    this.energyTier = energyTier;
  }

  @Override
  public boolean canBeDepleted() {
    return false;
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

  public ItemStack makeFullStack() {
    ItemStack full = new ItemStack(this);
    ModComponents.setEnergy(full, maxEnergy);
    return full;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return Math.round(13.0F - ((1 - ElectricItem.getChargeRatio(stack)) * 13.0F));
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return Mth.hsvToRgb(0, 1.0F, 1.0F);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    if (this instanceof com.faktocraft.common.interfaces.item.IArmorProperties armorProperties
        && armorProperties.supportsNightVision()) {
      tooltip.add(Component.translatable("tooltip." + com.faktocraft.Faktocraft.MODID + ".night_vision_cost",
          com.faktocraft.common.util.NightVisionHandler.COST_PER_SECOND).withStyle(ChatFormatting.GRAY));
    }
    tooltip.add(EnumLang.POWER_TIER.getTranslationComponent(
            energyTier.getLang().getTranslationComponent().withStyle(energyTier.getColor()))
        .withStyle(ChatFormatting.GRAY)
        .append(Component.literal(" (" + TextComponentUtil.getFormattedLong(energyTier.getBasicTransfer()) + " IE/t)")
            .withStyle(ChatFormatting.DARK_GRAY)));

    int energyStored = Mth.clamp(ModComponents.getEnergy(stack, initialEnergy), 0, maxEnergy);
    tooltip.add(EnumLang.STORED.getTranslationComponent(TextComponentUtil.build(
        EnumLang.POWER.getTranslationComponent(TextComponentUtil.getFormattedEnergyUnit(energyStored))
            .withStyle(energyTier.getColor()),
        Component.literal(" / ").withStyle(ChatFormatting.GRAY),
        EnumLang.POWER.getTranslationComponent(TextComponentUtil.getFormattedEnergyUnit(maxEnergy))
            .withStyle(energyTier.getColor()))).withStyle(ChatFormatting.GRAY));

    super.appendHoverText(stack, level, tooltip, flag);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
    if (slot == getType().getSlot() && !ModComponents.getActive(stack, true)) {
      return ImmutableMultimap.of();
    }
    return super.getAttributeModifiers(slot, stack);
  }

  @Override
  public void inventoryTick(ItemStack stack, Level level, Entity owner, int slotId, boolean isSelected) {
    if (!level.isClientSide()) {
      tickElectric(stack);
    }
  }

  @Override
  public void tickElectric(ItemStack stack) {
    boolean active = getEnergy(stack).energyStored() > 0;
    Boolean previous = ModComponents.hasActive(stack) ? ModComponents.getActive(stack, false) : null;
    if (previous == null || previous != active) {
      ModComponents.setActive(stack, active);
    }
  }
}
