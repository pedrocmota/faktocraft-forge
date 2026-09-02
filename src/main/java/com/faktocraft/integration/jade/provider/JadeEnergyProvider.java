package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class JadeEnergyProvider implements IBlockComponentProvider {

  public static final JadeEnergyProvider INSTANCE = new JadeEnergyProvider();

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "energy_info");

  private static final int FILLED_COLOR = 0xFF000000 | (76 << 16) | (178 << 8) | 13;

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    int maxEnergy = data.contains(JadeEnergyDataProvider.TAG_MAX_ENERGY)
        ? data.getInt(JadeEnergyDataProvider.TAG_MAX_ENERGY)
        : -1;
    if (maxEnergy <= 0) {
      if (data.contains(JadeEnergyDataProvider.TAG_TIER)) {
        appendTierLine(tooltip, data);
        tooltip.add(Component.translatable("gui." + Faktocraft.MODID + ".capacitor_required")
            .withStyle(ChatFormatting.RED));
      }
      return;
    }

    int energyStored = data.getInt(JadeEnergyDataProvider.TAG_ENERGY);
    float ratio = Math.min(1F, (float) energyStored / maxEnergy);

    Component barText = Component.literal(
        TextComponentUtil.getFormattedEnergyUnit(energyStored)
            + " / " + TextComponentUtil.getFormattedEnergyUnit(maxEnergy) + " IE");

    IElementHelper helper = IElementHelper.get();
    tooltip.add(helper.progress(ratio, barText, helper.progressStyle().color(FILLED_COLOR),
        BoxStyle.DEFAULT, true));

    appendTierLine(tooltip, data);

    if (data.getBoolean(JadeEnergyDataProvider.TAG_REDSTONE_OFF)) {
      tooltip.add(Component.translatable("top." + Faktocraft.MODID + ".redstone_off")
          .withStyle(ChatFormatting.GOLD));
    } else if (data.getInt(JadeEnergyDataProvider.TAG_ENERGY) <= 0) {
      if (data.getBoolean(JadeEnergyDataProvider.TAG_GENERATOR)) {
        tooltip.add(Component.translatable("top." + Faktocraft.MODID + ".buffer_empty")
            .withStyle(ChatFormatting.GRAY));
      } else {
        tooltip.add(Component.translatable("top." + Faktocraft.MODID + ".no_energy")
            .withStyle(ChatFormatting.RED));
      }
    } else if (data.getBoolean(JadeEnergyDataProvider.TAG_UNDERVOLTAGE)) {
      tooltip.add(Component.translatable("top." + Faktocraft.MODID + ".undervoltage")
          .withStyle(ChatFormatting.YELLOW));
    }
  }

  private static void appendTierLine(ITooltip tooltip, CompoundTag data) {
    int[] levels = data.contains(JadeEnergyDataProvider.TAG_TIERS)
        ? data.getIntArray(JadeEnergyDataProvider.TAG_TIERS)
        : new int[] {
          data.contains(JadeEnergyDataProvider.TAG_TIER) ? data.getInt(JadeEnergyDataProvider.TAG_TIER) : 1};
    if (levels.length == 0) {
      return;
    }

    MutableComponent tiers = Component.empty();
    for (int i = 0; i < levels.length; i++) {
      if (i > 0) {
        tiers.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
      }
      EnergyTier tier = EnergyTier.getTierFromLvl(levels[i]);
      tiers.append(Component.translatable(tier.getLang().getTranslationKey()).withStyle(tier.getColor()));
    }

    tooltip.add(Component.translatable("top." + Faktocraft.MODID + ".energy_tier", tiers)
        .withStyle(ChatFormatting.DARK_GRAY));
  }
}
