package com.faktocraft.common.radiation;

import net.minecraft.world.entity.LivingEntity;

public final class RadiationExposure {

  private static final String TAG = "FaktocraftRadiation";
  private static final String TAG_DAMAGE = "FaktocraftRadiationDamage";

  private RadiationExposure() {
  }

  public static float get(LivingEntity living) {
    return living.getPersistentData().getFloatOr(TAG, 0.0F);
  }

  public static float pendingDamage(LivingEntity living) {
    return living.getPersistentData().getFloatOr(TAG_DAMAGE, 0.0F);
  }

  public static void setPendingDamage(LivingEntity living, float damage) {
    if (damage <= 0.0F) {
      living.getPersistentData().remove(TAG_DAMAGE);
    } else {
      living.getPersistentData().putFloat(TAG_DAMAGE, damage);
    }
  }

  public static void set(LivingEntity living, float exposure) {
    if (exposure <= 0.0F) {
      living.getPersistentData().remove(TAG);
    } else {
      living.getPersistentData().putFloat(TAG, exposure);
    }
  }
}
