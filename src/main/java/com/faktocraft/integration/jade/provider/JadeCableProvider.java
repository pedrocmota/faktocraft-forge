package com.faktocraft.integration.jade.provider;

import com.faktocraft.IndReb;
import com.faktocraft.common.enums.EnergyTier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeCableProvider implements IBlockComponentProvider {

  public static final JadeCableProvider INSTANCE = new JadeCableProvider();

  private static final ResourceLocation UID = new ResourceLocation(IndReb.MODID, "cable_info");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    int cableTierLvl = data.contains(JadeCableDataProvider.TAG_CABLE_TIER)
        ? data.getInt(JadeCableDataProvider.TAG_CABLE_TIER)
        : -1;
    if (cableTierLvl < 0) {
      return;
    }

    int flowingLvl = data.contains(JadeCableDataProvider.TAG_FLOWING)
        ? data.getInt(JadeCableDataProvider.TAG_FLOWING)
        : -1;
    Component flowingComponent;
    if (flowingLvl >= 0) {
      EnergyTier flowing = EnergyTier.getTierFromLvl(flowingLvl);
      flowingComponent = Component.translatable(flowing.getLang().getTranslationKey()).withStyle(flowing.getColor());
    } else {
      flowingComponent = Component.literal("-");
    }
    tooltip.add(Component.translatable("top." + IndReb.MODID + ".current_voltage", flowingComponent)
        .withStyle(ChatFormatting.DARK_GRAY));

    EnergyTier cableTier = EnergyTier.getTierFromLvl(cableTierLvl);
    tooltip.add(Component.translatable("top." + IndReb.MODID + ".energy_tier",
        Component.translatable(cableTier.getLang().getTranslationKey()).withStyle(cableTier.getColor()))
        .withStyle(ChatFormatting.DARK_GRAY));
  }
}
