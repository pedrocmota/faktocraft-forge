package com.faktocraft.common.registries;

import com.faktocraft.common.effect.RadiationEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {

  public static final MobEffect RADIATION = RegistrationHandler.enqueue(Registries.MOB_EFFECT, "radiation",
      new RadiationEffect());

  private ModEffects() {
  }

  public static void register() {
  }
}
