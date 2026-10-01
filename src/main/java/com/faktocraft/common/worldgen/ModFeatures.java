package com.faktocraft.common.worldgen;

import com.faktocraft.common.registries.RegistrationHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;

public class ModFeatures {

  public static final MapCodec<GiantOilPocketFeature> GIANT_OIL_POCKET = RegistrationHandler
      .enqueue(Registries.FEATURE_TYPE, "giant_oil_pocket", GiantOilPocketFeature.CODEC);

  public static void register() {
  }
}
