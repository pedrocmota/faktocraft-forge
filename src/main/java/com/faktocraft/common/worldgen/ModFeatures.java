package com.faktocraft.common.worldgen;

import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ModFeatures {

  public static final Feature<NoneFeatureConfiguration> GIANT_OIL_POCKET = RegistrationHandler
      .enqueue(Registries.FEATURE, "giant_oil_pocket",
          new GiantOilPocketFeature(NoneFeatureConfiguration.CODEC));

  public static void register() {
  }
}
