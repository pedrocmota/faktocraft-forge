package com.faktocraft.common.enums;

import com.faktocraft.common.config.ModConfig;
import net.minecraft.ChatFormatting;

public enum EnergyTier {
  LOW(EnumLang.TIER_LOW, 1, ChatFormatting.GREEN),
  MEDIUM(EnumLang.TIER_MEDIUM, 2, ChatFormatting.YELLOW),
  HIGH(EnumLang.TIER_HIGH, 3, ChatFormatting.RED),
  VERY_HIGH(EnumLang.TIER_VERY_HIGH, 4, ChatFormatting.BLUE),
  ULTRA(EnumLang.TIER_ULTRA, 5, ChatFormatting.GOLD);

  private final EnumLang lang;
  private final int lvl;
  private final ChatFormatting color;

  EnergyTier(EnumLang lang, int lvl, ChatFormatting color) {
    this.lang = lang;
    this.lvl = lvl;
    this.color = color;
  }

  public EnumLang getLang() {
    return lang;
  }

  public int getLvl() {
    return lvl;
  }

  public ChatFormatting getColor() {
    return color;
  }

  public int getBasicTransfer() {
    return switch (this) {
      case LOW -> ModConfig.server().low_tier_transfer;
      case MEDIUM -> ModConfig.server().medium_tier_transfer;
      case HIGH -> ModConfig.server().high_tier_transfer;
      case VERY_HIGH -> ModConfig.server().very_high_tier_transfer;
      case ULTRA -> ModConfig.server().ultra_tier_transfer;
    };
  }

  public static EnergyTier getTierFromLvl(int lvl) {
    for (EnergyTier tier : values()) {
      if (tier.getLvl() == lvl) {
        return tier;
      }
    }
    return LOW;
  }
}
