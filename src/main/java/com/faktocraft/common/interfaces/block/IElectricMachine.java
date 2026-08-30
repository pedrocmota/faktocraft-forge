package com.faktocraft.common.interfaces.block;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;
import java.util.Set;

public interface IElectricMachine {

  EnergyTier getEnergyTier();

  default Set<EnergyTier> getEnergyTiers() {
    return Set.of(getEnergyTier());
  }

  static MutableComponent tierTooltip(Set<EnergyTier> tiers) {
    List<EnergyTier> ordered = tiers.stream()
        .sorted(java.util.Comparator.comparingInt(EnergyTier::getLvl))
        .toList();

    MutableComponent names = Component.empty();
    for (int i = 0; i < ordered.size(); i++) {
      if (i > 0) {
        names.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
      }
      EnergyTier tier = ordered.get(i);
      names.append(tier.getLang().getTranslationComponent().withStyle(tier.getColor()));
    }

    MutableComponent line = EnumLang.POWER_TIER.getTranslationComponent(names).withStyle(ChatFormatting.GRAY);
    if (ordered.size() == 1) {
      line.append(Component.literal(
          " (" + TextComponentUtil.getFormattedLong(ordered.get(0).getBasicTransfer()) + " IE/t)")
          .withStyle(ChatFormatting.DARK_GRAY));
    }
    return line;
  }
}
