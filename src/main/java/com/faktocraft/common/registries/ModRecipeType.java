package com.faktocraft.common.registries;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.AdvancedShapedRecipe;
import com.faktocraft.common.recipe.impl.AlloySmeltingRecipe;
import com.faktocraft.common.recipe.impl.CompressingRecipe;
import com.faktocraft.common.recipe.impl.CrushingRecipe;
import com.faktocraft.common.recipe.impl.CuttingRecipe;
import com.faktocraft.common.recipe.impl.ExtractingRecipe;
import com.faktocraft.common.recipe.impl.ExtrudingRecipe;
import com.faktocraft.common.recipe.impl.FluidEnrichingRecipe;
import com.faktocraft.common.recipe.impl.FluidExtrudingRecipe;
import com.faktocraft.common.recipe.impl.OreWashingRecipe;
import com.faktocraft.common.recipe.impl.PolymerizingRecipe;
import com.faktocraft.common.recipe.impl.RecyclingRecipe;
import com.faktocraft.common.recipe.impl.RollingRecipe;
import com.faktocraft.common.recipe.impl.SawingRecipe;
import com.faktocraft.common.recipe.impl.ScannerRecipe;
import com.faktocraft.common.recipe.impl.ScrapBoxRecipe;
import com.faktocraft.common.recipe.impl.ThermalCentrifugingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipeType {

  public static final RecipeType<CrushingRecipe> CRUSHING = registerType("crushing");
  public static final RecipeType<CompressingRecipe> COMPRESSING = registerType("compressing");
  public static final RecipeType<ExtractingRecipe> EXTRACTING = registerType("extracting");
  public static final RecipeType<SawingRecipe> SAWING = registerType("sawing");
  public static final RecipeType<FluidExtrudingRecipe> FLUID_EXTRUDING = registerType("fluid_extruding");
  public static final RecipeType<AlloySmeltingRecipe> ALLOY_SMELTING = registerType("alloy_smelting");
  public static final RecipeType<com.faktocraft.common.recipe.impl.CircuitAssemblingRecipe> CIRCUIT_ASSEMBLING =
      registerType(
          "circuit_assembling");
  public static final RecipeType<RecyclingRecipe> RECYCLING = registerType("recycling");
  public static final RecipeType<FluidEnrichingRecipe> FLUID_ENRICHING = registerType("fluid_enriching");
  public static final RecipeType<OreWashingRecipe> ORE_WASHING = registerType("ore_washing");
  public static final RecipeType<PolymerizingRecipe> POLYMERIZING = registerType("polymerizing");
  public static final RecipeType<ScrapBoxRecipe> SCRAP_BOX = registerType("scrap_box");
  public static final RecipeType<ThermalCentrifugingRecipe> THERMAL_CENTRIFUGING = registerType("thermal_centrifuging");
  public static final RecipeType<ScannerRecipe> SCANNER = registerType("scanner");
  public static final RecipeType<RollingRecipe> ROLLING = registerType("rolling");
  public static final RecipeType<CuttingRecipe> CUTTING = registerType("cutting");
  public static final RecipeType<ExtrudingRecipe> EXTRUDING = registerType("extruding");

  public static final RecipeType<AdvancedShapedRecipe> ADVANCED_SHAPED = registerType("advanced_shaped");

  private static <T extends Recipe<?>> RecipeType<T> registerType(String key) {
    return RegistrationHandler.recipeType(key, new RecipeType<T>() {
      @Override
      public String toString() {
        return Faktocraft.MODID + ":" + key;
      }
    });
  }

  public static void register() {
  }
}
