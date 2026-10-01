package com.faktocraft.common.tier;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.TransformerMode;
import net.minecraft.util.Mth;

public enum TransformerTier {
  LOW(EnergyTier.LOW, EnergyTier.MEDIUM, true),
  MEDIUM(EnergyTier.MEDIUM, EnergyTier.HIGH, true),
  HIGH(EnergyTier.HIGH, EnergyTier.VERY_HIGH, true),
  VERY_HIGH(EnergyTier.VERY_HIGH, EnergyTier.ULTRA, false);

  private final EnergyTier minTier;
  private final EnergyTier maxTier;
  private final boolean stepUpAllowed;

  TransformerTier(EnergyTier minTier, EnergyTier maxTier, boolean stepUpAllowed) {
    this.minTier = minTier;
    this.maxTier = maxTier;
    this.stepUpAllowed = stepUpAllowed;
  }

  public boolean isStepUpAllowed() {
    return stepUpAllowed;
  }

  public EnergyTier getMinTier() {
    return minTier;
  }

  public EnergyTier getMaxTier() {
    return maxTier;
  }

  public int getStepUpLossPercent() {
    int percent = switch (this) {
      case LOW -> ModConfig.server().low_transformer_step_up_loss_percent;
      case MEDIUM -> ModConfig.server().medium_transformer_step_up_loss_percent;
      case HIGH -> ModConfig.server().high_transformer_step_up_loss_percent;
      case VERY_HIGH -> 0;
    };
    return Mth.clamp(percent, 0, 99);
  }

  public int getStepDownLossPercent() {
    int percent = switch (this) {
      case LOW -> ModConfig.server().low_transformer_step_down_loss_percent;
      case MEDIUM -> ModConfig.server().medium_transformer_step_down_loss_percent;
      case HIGH -> ModConfig.server().high_transformer_step_down_loss_percent;
      case VERY_HIGH -> ModConfig.server().very_high_transformer_step_down_loss_percent;
    };
    return Mth.clamp(percent, 0, 99);
  }

  public int getLossPercent(TransformerMode mode) {
    return mode == TransformerMode.STEP_UP ? getStepUpLossPercent() : getStepDownLossPercent();
  }
}
