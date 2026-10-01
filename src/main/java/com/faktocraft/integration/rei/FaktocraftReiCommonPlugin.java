package com.faktocraft.integration.rei;

import com.faktocraft.common.recipe.impl.AlloySmeltingRecipe;
import com.faktocraft.common.recipe.impl.CircuitAssemblingRecipe;
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
import com.faktocraft.common.recipe.impl.UraniumCentrifugingRecipe;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.ModRecipeType;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.forge.REIPluginCommon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import java.util.ArrayList;
import java.util.List;

@REIPluginCommon
public class FaktocraftReiCommonPlugin implements REICommonPlugin {

  @Override
  public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
    registry.register(MachineDisplay.SERIALIZER_ID, MachineDisplay.SERIALIZER);
  }

  @Override
  public void registerItemComparators(ItemComparatorRegistry registry) {
    registry.registerComponents(ModItems.NANO_SABER, ModItems.NANO_HELMET, ModItems.NANO_CHESTPLATE,
        ModItems.NANO_LEGGINGS, ModItems.NANO_BOOTS, ModItems.FLUID_CELL, ModItems.ELECTRIC_HOE,
        ModItems.ELECTRIC_WRENCH, ModItems.ELECTRIC_TREETAP, ModItems.MULTI_TOOL, ModItems.MINING_DRILL,
        ModItems.DIAMOND_DRILL, ModItems.IRIDIUM_DRILL, ModItems.CHAINSAW, ModItems.DIAMOND_CHAINSAW,
        ModItems.IRIDIUM_CHAINSAW, ModItems.BATTERY, ModItems.ADVANCED_BATTERY, ModItems.MEDIUM_BATTERY,
        ModItems.ADVANCED_MEDIUM_BATTERY, ModItems.ENERGY_CRYSTAL, ModItems.LAPOTRON_CRYSTAL,
        ModItems.ADVANCED_ENERGY_CRYSTAL, ModItems.ADVANCED_LAPOTRON_CRYSTAL, ModItems.IRIDIUM_CRYSTAL,
        ModItems.CHARGING_BATTERY, ModItems.ADVANCED_CHARGING_BATTERY, ModItems.CHARGING_ENERGY_CRYSTAL,
        ModItems.CHARGING_LAPOTRON_CRYSTAL);
  }

  private static float scrapBoxTotalWeight() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) {
      return 0.0F;
    }
    List<ScrapBoxRecipe> recipes = new ArrayList<>();
    for (RecipeHolder<ScrapBoxRecipe> holder : server.getRecipeManager().recipeMap().byType(ModRecipeType.SCRAP_BOX)) {
      recipes.add(holder.value());
    }
    return ScrapBoxRecipe.getTotalWeight(recipes);
  }

  @Override
  public void registerDisplays(ServerDisplayRegistry registry) {
    registry.beginRecipeFiller(CrushingRecipe.class).filterType(ModRecipeType.CRUSHING).fill(ReiDisplays::crushing);
    registry.beginRecipeFiller(CompressingRecipe.class).filterType(ModRecipeType.COMPRESSING)
        .fill(ReiDisplays::compressing);
    registry.beginRecipeFiller(ExtractingRecipe.class).filterType(ModRecipeType.EXTRACTING)
        .fill(ReiDisplays::extracting);
    registry.beginRecipeFiller(FluidExtrudingRecipe.class).filterType(ModRecipeType.FLUID_EXTRUDING)
        .fill(ReiDisplays::fluidExtruding);
    registry.beginRecipeFiller(SawingRecipe.class).filterType(ModRecipeType.SAWING).fill(ReiDisplays::sawing);
    registry.beginRecipeFiller(AlloySmeltingRecipe.class).filterType(ModRecipeType.ALLOY_SMELTING)
        .fill(ReiDisplays::alloySmelting);
    registry.beginRecipeFiller(CircuitAssemblingRecipe.class).filterType(ModRecipeType.CIRCUIT_ASSEMBLING)
        .fill(ReiDisplays::circuitAssembling);
    registry.beginRecipeFiller(RecyclingRecipe.class).filterType(ModRecipeType.RECYCLING)
        .fill(ReiDisplays::recycling);
    registry.beginRecipeFiller(FluidEnrichingRecipe.class).filterType(ModRecipeType.FLUID_ENRICHING)
        .fill(ReiDisplays::fluidEnriching);
    registry.beginRecipeFiller(OreWashingRecipe.class).filterType(ModRecipeType.ORE_WASHING)
        .fill(ReiDisplays::oreWashing);
    registry.beginRecipeFiller(PolymerizingRecipe.class).filterType(ModRecipeType.POLYMERIZING)
        .fill(ReiDisplays::polymerizing);
    registry.beginRecipeFiller(ThermalCentrifugingRecipe.class).filterType(ModRecipeType.THERMAL_CENTRIFUGING)
        .fill(ReiDisplays::thermalCentrifuging);
    registry.beginRecipeFiller(UraniumCentrifugingRecipe.class).filterType(ModRecipeType.URANIUM_CENTRIFUGING)
        .fill(ReiDisplays::uraniumCentrifuging);
    registry.beginRecipeFiller(ScannerRecipe.class).filterType(ModRecipeType.SCANNER).fill(ReiDisplays::scanner);
    registry.beginRecipeFiller(RollingRecipe.class).filterType(ModRecipeType.ROLLING).fill(ReiDisplays::rolling);
    registry.beginRecipeFiller(CuttingRecipe.class).filterType(ModRecipeType.CUTTING).fill(ReiDisplays::cutting);
    registry.beginRecipeFiller(ExtrudingRecipe.class).filterType(ModRecipeType.EXTRUDING)
        .fill(ReiDisplays::extruding);

    float totalWeight = scrapBoxTotalWeight();
    registry.beginRecipeFiller(ScrapBoxRecipe.class).filterType(ModRecipeType.SCRAP_BOX)
        .fill(holder -> ReiDisplays.scrapBox(holder, totalWeight));

    registry.add(ReiDisplays.fermenting());
    registry.add(ReiDisplays.distilling());
    for (MachineDisplay display : ReiDisplays.matterFabricating()) {
      registry.add(display);
    }
  }
}
