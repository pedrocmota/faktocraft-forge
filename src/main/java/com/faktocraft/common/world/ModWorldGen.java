package com.faktocraft.common.world;

import com.faktocraft.common.registries.RegistrationHandler;
import com.faktocraft.common.world.feature.tree.RubberFoliagePlacer;
import com.faktocraft.common.world.feature.tree.RubberTrunkPlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class ModWorldGen {

  public static final FoliagePlacerType<RubberFoliagePlacer> RUBBER_FOLIAGE_PLACER = RegistrationHandler.enqueue(
      Registries.FOLIAGE_PLACER_TYPE, "rubber_foliage_placer",
      new FoliagePlacerType<>(RubberFoliagePlacer.CODEC));

  public static final TrunkPlacerType<RubberTrunkPlacer> RUBBER_TRUNK_PLACER = RegistrationHandler.enqueue(
      Registries.TRUNK_PLACER_TYPE, "rubber_trunk_placer",
      new TrunkPlacerType<>(RubberTrunkPlacer.CODEC));

  public static void register() {
  }
}
