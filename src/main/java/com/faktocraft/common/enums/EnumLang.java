package com.faktocraft.common.enums;

import com.faktocraft.Faktocraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum EnumLang {
  POWER("power", "energy"),
  POWER_TICK("power_tick", "energy"),

  TIER_LOW("low", "tier"),
  TIER_MEDIUM("medium", "tier"),
  TIER_HIGH("high", "tier"),
  TIER_VERY_HIGH("very_high", "tier"),
  TIER_ULTRA("ultra", "tier"),

  POWER_TIER("power_tier", "tooltip"),
  TRANSFER("transfer", "tooltip"),
  STORED("stored", "tooltip"),

  ARMOUR("armour", "gui"),
  REPLICATION_EMPTY("scanner.replication_empty", "gui"),
  MATTER_COST("scanner.matter_cost", "gui"),
  ENERGY_COST("scanner.energy_cost", "gui"),
  SUPPORTED_UPGRADES("supported_upgrades", "gui"),

  OVERCLOCKER_UPGRADE("overclocker_upgrade", "item"),
  EFFICIENCY_UPGRADE("efficiency_upgrade", "item"),
  TRANSFORMER_UPGRADE("transformer_upgrade", "item"),

  CLEAR_PATTERN("clear_pattern", "tooltip"),
  SAVE_PATTERN("save_pattern", "tooltip"),
  STOP_REPLICATION("stop_replication", "tooltip"),
  SINGLE_RUN("single_run", "tooltip"),
  REPEAT_RUN("repeat_run", "tooltip"),

  CHANCE("chance", "jei");

  private final String type;
  private final String path;

  EnumLang(String type, String path) {
    this.type = type;
    this.path = path;
  }

  public String getTranslationKey() {
    return getKey();
  }

  public String getKey() {
    if (path.isEmpty()) {
      return type;
    }
    return path + "." + Faktocraft.MODID + "." + type;
  }

  public MutableComponent getTranslationComponent() {
    return Component.translatable(getKey());
  }

  public MutableComponent getTranslationComponent(Object... args) {
    return Component.translatable(getKey(), args);
  }
}
