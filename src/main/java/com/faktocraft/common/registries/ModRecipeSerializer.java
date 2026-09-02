package com.faktocraft.common.registries;

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
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipeSerializer {

  public static final RecipeSerializer<CrushingRecipe> CRUSHING = register("crushing", CrushingRecipe.SERIALIZER);
  public static final RecipeSerializer<CompressingRecipe> COMPRESSING = register("compressing",
      CompressingRecipe.SERIALIZER);
  public static final RecipeSerializer<ExtractingRecipe> EXTRACTING = register("extracting",
      ExtractingRecipe.SERIALIZER);
  public static final RecipeSerializer<SawingRecipe> SAWING = register("sawing", SawingRecipe.SERIALIZER);
  public static final RecipeSerializer<FluidExtrudingRecipe> FLUID_EXTRUDING = register("fluid_extruding",
      FluidExtrudingRecipe.SERIALIZER);
  public static final RecipeSerializer<AlloySmeltingRecipe> ALLOY_SMELTING = register("alloy_smelting",
      AlloySmeltingRecipe.SERIALIZER);
  public static final RecipeSerializer<com.faktocraft.common.recipe.impl.CircuitAssemblingRecipe> CIRCUIT_ASSEMBLING =
      register(
          "circuit_assembling", com.faktocraft.common.recipe.impl.CircuitAssemblingRecipe.SERIALIZER);
  public static final RecipeSerializer<RecyclingRecipe> RECYCLING = register("recycling", RecyclingRecipe.SERIALIZER);
  public static final RecipeSerializer<FluidEnrichingRecipe> FLUID_ENRICHING = register("fluid_enriching",
      FluidEnrichingRecipe.SERIALIZER);
  public static final RecipeSerializer<ScrapBoxRecipe> SCRAP_BOX = register("scrap_box", ScrapBoxRecipe.SERIALIZER);
  public static final RecipeSerializer<OreWashingRecipe> ORE_WASHING = register("ore_washing",
      OreWashingRecipe.SERIALIZER);
  public static final RecipeSerializer<PolymerizingRecipe> POLYMERIZING = register("polymerizing",
      PolymerizingRecipe.SERIALIZER);
  public static final RecipeSerializer<ThermalCentrifugingRecipe> THERMAL_CENTRIFUGING = register(
      "thermal_centrifuging", ThermalCentrifugingRecipe.SERIALIZER);
  public static final RecipeSerializer<ScannerRecipe> SCANNER = register("scanner", ScannerRecipe.SERIALIZER);
  public static final RecipeSerializer<RollingRecipe> ROLLING = register("rolling", RollingRecipe.SERIALIZER);
  public static final RecipeSerializer<CuttingRecipe> CUTTING = register("cutting", CuttingRecipe.SERIALIZER);
  public static final RecipeSerializer<ExtrudingRecipe> EXTRUDING = register("extruding", ExtrudingRecipe.SERIALIZER);

  public static final RecipeSerializer<AdvancedShapedRecipe> ADVANCED_SHAPED = register("advanced_shaped",
      AdvancedShapedRecipe.SERIALIZER);

  private static <T extends Recipe<?>> RecipeSerializer<T> register(String key, RecipeSerializer<T> serializer) {
    return RegistrationHandler.recipeSerializer(key, serializer);
  }

  public static void register() {
  }
}
