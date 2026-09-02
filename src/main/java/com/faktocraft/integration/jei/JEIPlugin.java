package com.faktocraft.integration.jei;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.alloy_smelter.BlockEntityAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.MenuAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenAlloySmelter;
import com.faktocraft.common.block.impl.machines.circuit_assembler.BlockEntityCircuitAssembler;
import com.faktocraft.common.block.impl.machines.circuit_assembler.MenuCircuitAssembler;
import com.faktocraft.common.block.impl.machines.circuit_assembler.ScreenCircuitAssembler;
import com.faktocraft.common.block.impl.machines.compressor.BlockEntityCompressor;
import com.faktocraft.common.block.impl.machines.compressor.MenuCompressor;
import com.faktocraft.common.block.impl.machines.compressor.ScreenCompressor;
import com.faktocraft.common.block.impl.machines.crusher.BlockEntityCrusher;
import com.faktocraft.common.block.impl.machines.crusher.MenuCrusher;
import com.faktocraft.common.block.impl.machines.crusher.ScreenCrusher;
import com.faktocraft.common.block.impl.machines.distillery.BlockEntityDistillery;
import com.faktocraft.common.block.impl.machines.electric_furnace.ScreenElectricFurnace;
import com.faktocraft.common.block.impl.machines.extractor.BlockEntityExtractor;
import com.faktocraft.common.block.impl.machines.extractor.MenuExtractor;
import com.faktocraft.common.block.impl.machines.extractor.ScreenExtractor;
import com.faktocraft.common.block.impl.machines.extruder.ScreenExtruder;
import com.faktocraft.common.block.impl.machines.fermenter.BlockEntityFermenter;
import com.faktocraft.common.block.impl.machines.fermenter.ScreenFermenter;
import com.faktocraft.common.block.impl.machines.fluid_enricher.BlockEntityFluidEnricher;
import com.faktocraft.common.block.impl.machines.fluid_enricher.MenuFluidEnricher;
import com.faktocraft.common.block.impl.machines.fluid_enricher.ScreenFluidEnricher;
import com.faktocraft.common.block.impl.machines.iron_furnace.ScreenIronFurnace;
import com.faktocraft.common.block.impl.machines.matter_fabricator.BlockEntityMatterFabricator;
import com.faktocraft.common.block.impl.machines.matter_fabricator.ScreenMatterFabricator;
import com.faktocraft.common.block.impl.machines.metal_former.BlockEntityMetalFormer;
import com.faktocraft.common.block.impl.machines.metal_former.MenuMetalFormer;
import com.faktocraft.common.block.impl.machines.metal_former.ScreenMetalFormer;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.BlockEntityOreWashingPlant;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.MenuOreWashingPlant;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.ScreenOreWashingPlant;
import com.faktocraft.common.block.impl.machines.polymerizer.BlockEntityPolymerizer;
import com.faktocraft.common.block.impl.machines.polymerizer.MenuPolymerizer;
import com.faktocraft.common.block.impl.machines.polymerizer.ScreenPolymerizer;
import com.faktocraft.common.block.impl.machines.recycler.BlockEntityRecycler;
import com.faktocraft.common.block.impl.machines.recycler.MenuRecycler;
import com.faktocraft.common.block.impl.machines.recycler.ScreenRecycler;
import com.faktocraft.common.block.impl.machines.sawmill.BlockEntitySawmill;
import com.faktocraft.common.block.impl.machines.sawmill.MenuSawmill;
import com.faktocraft.common.block.impl.machines.sawmill.ScreenSawmill;
import com.faktocraft.common.block.impl.machines.scanner.BlockEntityScanner;
import com.faktocraft.common.block.impl.machines.scanner.MenuScanner;
import com.faktocraft.common.block.impl.machines.scanner.ScreenScanner;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.BlockEntityThermalCentrifuge;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.MenuThermalCentrifuge;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.ScreenThermalCentrifuge;
import com.faktocraft.common.recipe.impl.ScrapBoxRecipe;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.ModRecipeType;
import com.faktocraft.common.registries.machines.M2Registry;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.screen.PanelScreen;
import com.faktocraft.integration.jei.category.impl.AlloySmeltingCategory;
import com.faktocraft.integration.jei.category.impl.CircuitAssemblingCategory;
import com.faktocraft.integration.jei.category.impl.CompressingCategory;
import com.faktocraft.integration.jei.category.impl.CrushingCategory;
import com.faktocraft.integration.jei.category.impl.CuttingCategory;
import com.faktocraft.integration.jei.category.impl.DistillingCategory;
import com.faktocraft.integration.jei.category.impl.ExtractingCategory;
import com.faktocraft.integration.jei.category.impl.ExtrudingCategory;
import com.faktocraft.integration.jei.category.impl.FermentingCategory;
import com.faktocraft.integration.jei.category.impl.FluidEnrichingCategory;
import com.faktocraft.integration.jei.category.impl.FluidExtrudingCategory;
import com.faktocraft.integration.jei.category.impl.MatterFabricatingCategory;
import com.faktocraft.integration.jei.category.impl.OreWashingCategory;
import com.faktocraft.integration.jei.category.impl.PolymerizingCategory;
import com.faktocraft.integration.jei.category.impl.RecyclingCategory;
import com.faktocraft.integration.jei.category.impl.RollingCategory;
import com.faktocraft.integration.jei.category.impl.SawingCategory;
import com.faktocraft.integration.jei.category.impl.ScannerCategory;
import com.faktocraft.integration.jei.category.impl.ScrapBoxCategory;
import com.faktocraft.integration.jei.category.impl.ThermalCentrifugingCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, Faktocraft.MODID);

  @Override
  public ResourceLocation getPluginUid() {
    return UID;
  }

  @org.jetbrains.annotations.Nullable
  private static mezz.jei.api.runtime.IJeiRuntime activeRuntime;

  @Override
  public void onRuntimeUnavailable() {
    activeRuntime = null;
  }

  @Override
  public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
    activeRuntime = runtime;
    java.util.List<net.minecraft.world.item.ItemStack> unreleased = new java.util.ArrayList<>();
    for (net.minecraft.world.item.Item item : ModItems.getAllItems()) {
      if (com.faktocraft.common.registries.ModCreativeTab.isUnreleased(item)) {
        unreleased.add(new net.minecraft.world.item.ItemStack(item));
      }
    }
    if (!unreleased.isEmpty()) {
      runtime.getIngredientManager().removeIngredientsAtRuntime(
          mezz.jei.api.constants.VanillaTypes.ITEM_STACK, unreleased);
    }
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

    registration.addRecipeCategories(new CrushingCategory(guiHelper));
    registration.addRecipeCategories(new CompressingCategory(guiHelper));
    registration.addRecipeCategories(new ExtractingCategory(guiHelper));
    registration.addRecipeCategories(new FluidExtrudingCategory(guiHelper));
    registration.addRecipeCategories(new SawingCategory(guiHelper));
    registration.addRecipeCategories(new AlloySmeltingCategory(guiHelper));
    registration.addRecipeCategories(new CircuitAssemblingCategory(guiHelper));
    registration.addRecipeCategories(new RecyclingCategory(guiHelper));
    registration.addRecipeCategories(new FluidEnrichingCategory(guiHelper));
    registration.addRecipeCategories(new OreWashingCategory(guiHelper));
    registration.addRecipeCategories(new PolymerizingCategory(guiHelper));
    registration.addRecipeCategories(new ThermalCentrifugingCategory(guiHelper));
    registration.addRecipeCategories(new ScannerCategory(guiHelper));
    registration.addRecipeCategories(new ScrapBoxCategory(guiHelper));
    registration.addRecipeCategories(new RollingCategory(guiHelper));
    registration.addRecipeCategories(new CuttingCategory(guiHelper));
    registration.addRecipeCategories(new ExtrudingCategory(guiHelper));
    registration.addRecipeCategories(new FermentingCategory(guiHelper));
    registration.addRecipeCategories(new DistillingCategory(guiHelper));
    registration.addRecipeCategories(new MatterFabricatingCategory(guiHelper));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    long start = System.nanoTime();

    Minecraft minecraft = Minecraft.getInstance();
    ClientLevel level = minecraft.level;
    if (level == null) {
      Faktocraft.LOGGER.warn("JEI recipe registration ran without a client level; Faktocraft recipes skipped");
      return;
    }
    RecipeManager recipeManager = level.getRecipeManager();

    registration.addRecipes(CrushingCategory.TYPE, recipes(recipeManager, ModRecipeType.CRUSHING));
    registration.addRecipes(CompressingCategory.TYPE, recipes(recipeManager, ModRecipeType.COMPRESSING));
    registration.addRecipes(ExtractingCategory.TYPE, recipes(recipeManager, ModRecipeType.EXTRACTING));
    registration.addRecipes(FluidExtrudingCategory.TYPE, recipes(recipeManager, ModRecipeType.FLUID_EXTRUDING));
    registration.addRecipes(SawingCategory.TYPE, recipes(recipeManager, ModRecipeType.SAWING));
    registration.addRecipes(AlloySmeltingCategory.TYPE, recipes(recipeManager, ModRecipeType.ALLOY_SMELTING));
    registration.addRecipes(CircuitAssemblingCategory.TYPE, recipes(recipeManager, ModRecipeType.CIRCUIT_ASSEMBLING));
    Faktocraft.LOGGER.info("JEI sync check: alloy_smelting={} circuit_assembling={}",
        recipes(recipeManager, ModRecipeType.ALLOY_SMELTING).size(),
        recipes(recipeManager, ModRecipeType.CIRCUIT_ASSEMBLING).size());
    registration.addRecipes(RecyclingCategory.TYPE, recipes(recipeManager, ModRecipeType.RECYCLING));
    registration.addRecipes(FluidEnrichingCategory.TYPE, recipes(recipeManager, ModRecipeType.FLUID_ENRICHING));
    registration.addRecipes(OreWashingCategory.TYPE, recipes(recipeManager, ModRecipeType.ORE_WASHING));
    registration.addRecipes(PolymerizingCategory.TYPE, recipes(recipeManager, ModRecipeType.POLYMERIZING));
    registration.addRecipes(ThermalCentrifugingCategory.TYPE,
        recipes(recipeManager, ModRecipeType.THERMAL_CENTRIFUGING));
    registration.addRecipes(ScannerCategory.TYPE, recipes(recipeManager, ModRecipeType.SCANNER));
    registration.addRecipes(RollingCategory.TYPE, recipes(recipeManager, ModRecipeType.ROLLING));
    registration.addRecipes(CuttingCategory.TYPE, recipes(recipeManager, ModRecipeType.CUTTING));
    registration.addRecipes(ExtrudingCategory.TYPE, recipes(recipeManager, ModRecipeType.EXTRUDING));

    registration.addRecipes(FermentingCategory.TYPE, List.of(fermentingEntry()));
    registration.addRecipes(DistillingCategory.TYPE, List.of(distillingEntry()));
    registration.addRecipes(MatterFabricatingCategory.TYPE, matterFabricatingEntries());

    registration.addIngredientInfo(ModItems.FERTILIZER,
        net.minecraft.network.chat.Component.translatable("jei." + Faktocraft.MODID + ".fertilizer.info",
            BlockEntityFermenter.WASTE_EVERY_TICKS / 20));

    List<ScrapBoxRecipe> scrapBoxRecipes = recipeManager.getAllRecipesFor(ModRecipeType.SCRAP_BOX);
    ScrapBoxCategory.setTotalWeight(ScrapBoxRecipe.getTotalWeight(scrapBoxRecipes));
    registration.addRecipes(ScrapBoxCategory.TYPE, scrapBoxRecipes);

    Faktocraft.LOGGER.info("Loaded JEI recipe integration in {} ms", (System.nanoTime() - start) / 1_000_000);
  }

  private static FermentingCategory.Entry fermentingEntry() {
    return new FermentingCategory.Entry(
        new net.minecraft.world.item.ItemStack(ModItems.MUD_PILE, BlockEntityFermenter.MUD_PER_OP),
        new net.minecraftforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.BIOMASS.still(),
            BlockEntityFermenter.BIOMASS_PER_OP),
        new net.minecraftforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.BIOGAS.still(),
            BlockEntityFermenter.BIOGAS_PER_OP),
        BlockEntityFermenter.DURATION_TICKS,
        com.faktocraft.common.config.ModConfig.server().fermenter_tick_usage);
  }

  private static DistillingCategory.Entry distillingEntry() {
    return new DistillingCategory.Entry(
        new net.minecraftforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.OIL.still(),
            BlockEntityDistillery.OIL_PER_OP),
        new net.minecraftforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.SULFURIC_ACID.still(),
            BlockEntityDistillery.ACID_PER_OP),
        new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,
            BlockEntityDistillery.WATER_PER_OP),
        new net.minecraftforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.FUEL.still(),
            BlockEntityDistillery.FUEL_PER_OP),
        new net.minecraft.world.item.ItemStack(ModItems.SULFUR_DUST),
        BlockEntityDistillery.SULFUR_CHANCE,
        BlockEntityDistillery.DURATION_TICKS,
        BlockEntityDistillery.POWER_PER_TICK);
  }

  private static List<MatterFabricatingCategory.Entry> matterFabricatingEntries() {
    net.minecraftforge.fluids.FluidStack matter = new net.minecraftforge.fluids.FluidStack(
        com.faktocraft.common.fluid.ModFluids.MATTER.still(),
        com.faktocraft.common.config.ModConfig.server().matter_fabricator_produce_run);

    int baseCost = BlockEntityMatterFabricator.PROGRESS_TARGET;
    int amplifiedCost = net.minecraft.util.Mth.ceil(
        baseCost / (float) (1 + BlockEntityMatterFabricator.AMPLIFIER_BONUS));
    int scrapCount = net.minecraft.util.Mth.ceil(
        amplifiedCost / (float) BlockEntityMatterFabricator.SCRAP_AMPLIFIER);
    int scrapBoxCount = net.minecraft.util.Mth.ceil(
        amplifiedCost / (float) BlockEntityMatterFabricator.SCRAP_BOX_AMPLIFIER);

    return List.of(
        new MatterFabricatingCategory.Entry(
            new net.minecraft.world.item.ItemStack(ModItems.SCRAP, scrapCount), amplifiedCost, matter),
        new MatterFabricatingCategory.Entry(
            new net.minecraft.world.item.ItemStack(ModItems.SCRAP_BOX, scrapBoxCount), amplifiedCost, matter));
  }

  private static <C extends Container, T extends Recipe<C>> List<T> recipes(RecipeManager recipeManager,
      RecipeType<T> type) {
    return recipeManager.getAllRecipesFor(type);
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalysts(RecipeTypes.SMELTING, M2Registry.IRON_FURNACE, M2Registry.ELECTRIC_FURNACE);

    registration.addRecipeCatalysts(CrushingCategory.TYPE, M2Registry.CRUSHER);
    registration.addRecipeCatalysts(CompressingCategory.TYPE, M2Registry.COMPRESSOR);
    registration.addRecipeCatalysts(ExtractingCategory.TYPE, M2Registry.EXTRACTOR);
    registration.addRecipeCatalysts(FluidExtrudingCategory.TYPE, M3Registry.EXTRUDER);
    registration.addRecipeCatalysts(SawingCategory.TYPE, M2Registry.SAWMILL);
    registration.addRecipeCatalysts(AlloySmeltingCategory.TYPE, M3Registry.ALLOY_SMELTER);
    registration.addRecipeCatalysts(CircuitAssemblingCategory.TYPE, M3Registry.CIRCUIT_ASSEMBLER);
    registration.addRecipeCatalysts(RecyclingCategory.TYPE, M2Registry.RECYCLER);
    registration.addRecipeCatalysts(FluidEnrichingCategory.TYPE, M3Registry.FLUID_ENRICHER);
    registration.addRecipeCatalysts(OreWashingCategory.TYPE, M3Registry.ORE_WASHING_PLANT);
    registration.addRecipeCatalysts(PolymerizingCategory.TYPE, M3Registry.POLYMERIZER);
    registration.addRecipeCatalysts(ThermalCentrifugingCategory.TYPE, M3Registry.THERMAL_CENTRIFUGE);
    registration.addRecipeCatalysts(RollingCategory.TYPE, M3Registry.METAL_FORMER);
    registration.addRecipeCatalysts(CuttingCategory.TYPE, M3Registry.METAL_FORMER);
    registration.addRecipeCatalysts(ExtrudingCategory.TYPE, M3Registry.METAL_FORMER);
    registration.addRecipeCatalysts(FermentingCategory.TYPE, M3Registry.FERMENTER);
    registration.addRecipeCatalysts(DistillingCategory.TYPE,
        com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry.DISTILLERY);
    registration.addRecipeCatalysts(MatterFabricatingCategory.TYPE, M4Registry.MATTER_FABRICATOR);
    registration.addRecipeCatalysts(RecipeTypes.CRAFTING,
        com.faktocraft.common.block.impl.logistics.LogisticsRegistry.REQUEST_TABLE_ITEM,
        com.faktocraft.common.block.impl.logistics.LogisticsRegistry.ASSEMBLY_TABLE_ITEM);
  }

  private static mezz.jei.api.gui.handlers.IGuiClickableArea recipeArea(int x, int y,
      java.util.function.Supplier<net.minecraft.world.item.crafting.CraftingRecipe> current) {
    net.minecraft.client.renderer.Rect2i area = new net.minecraft.client.renderer.Rect2i(x, y, 24, 16);
    return new mezz.jei.api.gui.handlers.IGuiClickableArea() {
      @Override
      public net.minecraft.client.renderer.Rect2i getArea() {
        return area;
      }

      @Override
      public void onClick(mezz.jei.api.recipe.IFocusFactory focusFactory,
          mezz.jei.api.runtime.IRecipesGui recipesGui) {
        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        net.minecraft.world.item.crafting.CraftingRecipe recipe = current.get();
        if (recipe == null || level == null || activeRuntime == null) {
          recipesGui.showTypes(List.of(RecipeTypes.CRAFTING));
          return;
        }
        List<mezz.jei.api.recipe.IFocus<?>> focuses = List.of(focusFactory.createFocus(
            mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT,
            mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
            recipe.getResultItem(level.registryAccess())));
        mezz.jei.api.recipe.IRecipeManager manager = activeRuntime.getRecipeManager();

        List<net.minecraft.world.item.crafting.CraftingRecipe> found = manager.createRecipeLookup(RecipeTypes.CRAFTING)
            .limitFocus(focuses).get().toList();
        List<net.minecraft.world.item.crafting.CraftingRecipe> ordered = new java.util.ArrayList<>();
        found.stream().filter(other -> other.getId().equals(recipe.getId())).forEach(ordered::add);
        found.stream().filter(other -> !other.getId().equals(recipe.getId())).forEach(ordered::add);
        if (ordered.isEmpty()) {
          recipesGui.show(focuses);
          return;
        }
        recipesGui.showRecipes(manager.getRecipeCategory(RecipeTypes.CRAFTING), ordered, focuses);
      }
    };
  }

  private static mezz.jei.api.gui.handlers.IGuiClickableArea focusArea(int x, int y,
      mezz.jei.api.recipe.RecipeIngredientRole role, net.minecraft.world.item.ItemStack stack) {
    net.minecraft.client.renderer.Rect2i area = new net.minecraft.client.renderer.Rect2i(x, y, 24, 16);
    return new mezz.jei.api.gui.handlers.IGuiClickableArea() {
      @Override
      public net.minecraft.client.renderer.Rect2i getArea() {
        return area;
      }

      @Override
      public void onClick(mezz.jei.api.recipe.IFocusFactory focusFactory,
          mezz.jei.api.runtime.IRecipesGui recipesGui) {
        recipesGui.show(focusFactory.createFocus(role,
            mezz.jei.api.constants.VanillaTypes.ITEM_STACK, stack));
      }
    };
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGenericGuiContainerHandler(PanelScreen.class, new GuiHandler());

    registration.addRecipeClickArea(ScreenIronFurnace.class, 80, 35, 24, 16, RecipeTypes.SMELTING);
    registration.addRecipeClickArea(ScreenElectricFurnace.class, 71, 35, 24, 16, RecipeTypes.SMELTING);
    registration.addRecipeClickArea(ScreenCrusher.class, 71, 35, 24, 16, CrushingCategory.TYPE);
    registration.addRecipeClickArea(ScreenCompressor.class, 71, 35, 24, 16, CompressingCategory.TYPE);
    registration.addRecipeClickArea(ScreenExtractor.class, 71, 35, 24, 16, ExtractingCategory.TYPE);
    registration.addRecipeClickArea(ScreenExtruder.class, 78, 35, 24, 16, FluidExtrudingCategory.TYPE);
    registration.addRecipeClickArea(ScreenSawmill.class, 71, 35, 24, 16, SawingCategory.TYPE);
    registration.addRecipeClickArea(ScreenAlloySmelter.class, 82, 33, 24, 16, AlloySmeltingCategory.TYPE);
    registration.addRecipeClickArea(ScreenCircuitAssembler.class, 81, 33, 24, 16, CircuitAssemblingCategory.TYPE);
    registration.addRecipeClickArea(ScreenRecycler.class, 71, 35, 24, 16, RecyclingCategory.TYPE);
    registration.addRecipeClickArea(ScreenFluidEnricher.class, 76, 35, 24, 16, FluidEnrichingCategory.TYPE);
    registration.addRecipeClickArea(ScreenOreWashingPlant.class, 90, 32, 19, 19, OreWashingCategory.TYPE);
    registration.addRecipeClickArea(ScreenPolymerizer.class, 88, 35, 24, 16, PolymerizingCategory.TYPE);
    registration.addRecipeClickArea(ScreenThermalCentrifuge.class, 82, 33, 24, 16, ThermalCentrifugingCategory.TYPE);
    registration.addRecipeClickArea(ScreenFermenter.class, 76, 35, 24, 16, FermentingCategory.TYPE);
    registration.addRecipeClickArea(com.faktocraft.common.block.impl.machines.distillery.ScreenDistillery.class,
        94, 43, 24, 16, DistillingCategory.TYPE);
    registration.addRecipeClickArea(ScreenMatterFabricator.class, 104, 51, 24, 16, MatterFabricatingCategory.TYPE);

    registration.addRecipeClickArea(ScreenScanner.class, 7, 15, 61, 9, ScannerCategory.TYPE);
    registration.addRecipeClickArea(ScreenScanner.class, 51, 15, 18, 9, ScannerCategory.TYPE);
    registration.addRecipeClickArea(ScreenScanner.class, 7, 24, 18, 26, ScannerCategory.TYPE);
    registration.addRecipeClickArea(ScreenScanner.class, 51, 24, 18, 26, ScannerCategory.TYPE);
    registration.addRecipeClickArea(ScreenScanner.class, 7, 50, 61, 9, ScannerCategory.TYPE);

    registration.addRecipeClickArea(ScreenMetalFormer.class, 71, 34, 24, 18, RollingCategory.TYPE, CuttingCategory.TYPE,
        ExtrudingCategory.TYPE);

    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.quarry.ScreenQuarry.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public List<net.minecraft.client.renderer.Rect2i> getGuiExtraAreas(
              com.faktocraft.common.block.impl.quarry.ScreenQuarry screen) {
            return screen.extraAreas();
          }
        });
    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.logistics.ScreenChassis.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public List<net.minecraft.client.renderer.Rect2i> getGuiExtraAreas(
              com.faktocraft.common.block.impl.logistics.ScreenChassis screen) {
            return screen.extraAreas();
          }
        });
    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.pipe.ScreenExtractorPipe.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public List<net.minecraft.client.renderer.Rect2i> getGuiExtraAreas(
              com.faktocraft.common.block.impl.pipe.ScreenExtractorPipe screen) {
            return screen.extraAreas();
          }
        });
    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.logistics.ScreenAssemblyTable.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public java.util.Collection<mezz.jei.api.gui.handlers.IGuiClickableArea> getGuiClickableAreas(
              com.faktocraft.common.block.impl.logistics.ScreenAssemblyTable screen,
              double guiMouseX, double guiMouseY) {
            if (!screen.showsRecipe()) {
              return List.of();
            }
            return List.of(recipeArea(
                com.faktocraft.common.block.impl.logistics.MenuAssemblyTable.ARROW_X,
                com.faktocraft.common.block.impl.logistics.MenuAssemblyTable.ARROW_Y, screen::currentRecipe));
          }
        });
    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.logistics.ScreenCraftPipe.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public java.util.Collection<mezz.jei.api.gui.handlers.IGuiClickableArea> getGuiClickableAreas(
              com.faktocraft.common.block.impl.logistics.ScreenCraftPipe screen,
              double guiMouseX, double guiMouseY) {
            if (!screen.isEditing()) {
              return List.of();
            }
            return List.of(recipeArea(
                com.faktocraft.common.block.impl.logistics.ScreenCraftPipe.ARROW_X,
                com.faktocraft.common.block.impl.logistics.ScreenCraftPipe.ARROW_Y, screen::currentRecipe));
          }
        });
    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.logistics.ScreenRecipePipe.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public java.util.Collection<mezz.jei.api.gui.handlers.IGuiClickableArea> getGuiClickableAreas(
              com.faktocraft.common.block.impl.logistics.ScreenRecipePipe screen,
              double guiMouseX, double guiMouseY) {
            if (!screen.isEditing()) {
              return List.of();
            }
            mezz.jei.api.recipe.RecipeIngredientRole role = mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT;
            net.minecraft.world.item.ItemStack stack = screen.declaredOutput();
            if (stack.isEmpty()) {

              role = mezz.jei.api.recipe.RecipeIngredientRole.CATALYST;
              stack = screen.dockedStack();
            }
            if (stack.isEmpty()) {
              role = mezz.jei.api.recipe.RecipeIngredientRole.INPUT;
              stack = screen.declaredInput();
            }
            if (stack.isEmpty()) {
              return List.of();
            }
            return List.of(focusArea(
                com.faktocraft.common.block.impl.logistics.ScreenRecipePipe.ARROW_X,
                screen.arrowY(), role, stack));
          }
        });
    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.logistics.ScreenRequestTable.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public java.util.Collection<mezz.jei.api.gui.handlers.IGuiClickableArea> getGuiClickableAreas(
              com.faktocraft.common.block.impl.logistics.ScreenRequestTable screen,
              double guiMouseX, double guiMouseY) {
            if (!screen.isRequestsTab()) {
              return List.of();
            }
            return List.of(mezz.jei.api.gui.handlers.IGuiClickableArea.createBasic(
                66, 52, 28, 23, RecipeTypes.CRAFTING));
          }
        });
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(new CraftPipePatternTransferHandler(), RecipeTypes.CRAFTING);
    registration.addUniversalRecipeTransferHandler(
        new RecipePipeTransferHandler(registration.getTransferHelper()));
    registration.addRecipeTransferHandler(
        com.faktocraft.common.block.impl.logistics.MenuRequestTable.class,
        com.faktocraft.common.block.impl.logistics.LogisticsRegistry.REQUEST_TABLE_MENU,
        RecipeTypes.CRAFTING,
        com.faktocraft.common.block.impl.logistics.MenuRequestTable.MATRIX_START, 9,
        com.faktocraft.common.block.impl.logistics.MenuRequestTable.PLAYER_START, 36);
    registration.addRecipeTransferHandler(MenuCrusher.class, M2Registry.CRUSHER_MENU, CrushingCategory.TYPE,
        BlockEntityCrusher.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuCompressor.class, M2Registry.COMPRESSOR_MENU, CompressingCategory.TYPE,
        BlockEntityCompressor.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuExtractor.class, M2Registry.EXTRACTOR_MENU, ExtractingCategory.TYPE,
        BlockEntityExtractor.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuSawmill.class, M2Registry.SAWMILL_MENU, SawingCategory.TYPE,
        BlockEntitySawmill.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuAlloySmelter.class, M3Registry.ALLOY_SMELTER_MENU,
        AlloySmeltingCategory.TYPE, BlockEntityAlloySmelter.INPUT_SLOT_0, 3, 0, 37);
    registration.addRecipeTransferHandler(MenuCircuitAssembler.class, M3Registry.CIRCUIT_ASSEMBLER_MENU,
        CircuitAssemblingCategory.TYPE, BlockEntityCircuitAssembler.INPUT_SLOT_0, 3, 0, 37);
    registration.addRecipeTransferHandler(MenuRecycler.class, M2Registry.RECYCLER_MENU, RecyclingCategory.TYPE,
        BlockEntityRecycler.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuFluidEnricher.class, M3Registry.FLUID_ENRICHER_MENU,
        FluidEnrichingCategory.TYPE, BlockEntityFluidEnricher.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuOreWashingPlant.class, M3Registry.ORE_WASHING_PLANT_MENU,
        OreWashingCategory.TYPE, BlockEntityOreWashingPlant.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuPolymerizer.class, M3Registry.POLYMERIZER_MENU,
        PolymerizingCategory.TYPE, BlockEntityPolymerizer.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuThermalCentrifuge.class, M3Registry.THERMAL_CENTRIFUGE_MENU,
        ThermalCentrifugingCategory.TYPE, BlockEntityThermalCentrifuge.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuScanner.class, M4Registry.SCANNER_MENU, ScannerCategory.TYPE,
        BlockEntityScanner.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuMetalFormer.class, M3Registry.METAL_FORMER_MENU, RollingCategory.TYPE,
        BlockEntityMetalFormer.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuMetalFormer.class, M3Registry.METAL_FORMER_MENU, CuttingCategory.TYPE,
        BlockEntityMetalFormer.INPUT_SLOT, 1, 1, 37);
    registration.addRecipeTransferHandler(MenuMetalFormer.class, M3Registry.METAL_FORMER_MENU, ExtrudingCategory.TYPE,
        BlockEntityMetalFormer.INPUT_SLOT, 1, 1, 37);
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    registration.useNbtForSubtypes(ModItems.NANO_SABER);
    registration.useNbtForSubtypes(ModItems.NANO_HELMET);
    registration.useNbtForSubtypes(ModItems.NANO_CHESTPLATE);
    registration.useNbtForSubtypes(ModItems.NANO_LEGGINGS);
    registration.useNbtForSubtypes(ModItems.NANO_BOOTS);

    registration.useNbtForSubtypes(ModItems.FLUID_CELL);

    registration.useNbtForSubtypes(ModItems.ELECTRIC_HOE);
    registration.useNbtForSubtypes(ModItems.ELECTRIC_WRENCH);
    registration.useNbtForSubtypes(ModItems.ELECTRIC_TREETAP);
    registration.useNbtForSubtypes(ModItems.MULTI_TOOL);

    registration.useNbtForSubtypes(ModItems.MINING_DRILL);
    registration.useNbtForSubtypes(ModItems.DIAMOND_DRILL);
    registration.useNbtForSubtypes(ModItems.IRIDIUM_DRILL);

    registration.useNbtForSubtypes(ModItems.CHAINSAW);
    registration.useNbtForSubtypes(ModItems.DIAMOND_CHAINSAW);
    registration.useNbtForSubtypes(ModItems.IRIDIUM_CHAINSAW);

    registration.useNbtForSubtypes(ModItems.BATTERY);
    registration.useNbtForSubtypes(ModItems.ADVANCED_BATTERY);
    registration.useNbtForSubtypes(ModItems.MEDIUM_BATTERY);
    registration.useNbtForSubtypes(ModItems.ADVANCED_MEDIUM_BATTERY);
    registration.useNbtForSubtypes(ModItems.ENERGY_CRYSTAL);
    registration.useNbtForSubtypes(ModItems.LAPOTRON_CRYSTAL);
    registration.useNbtForSubtypes(ModItems.ADVANCED_ENERGY_CRYSTAL);
    registration.useNbtForSubtypes(ModItems.ADVANCED_LAPOTRON_CRYSTAL);
    registration.useNbtForSubtypes(ModItems.IRIDIUM_CRYSTAL);

    registration.useNbtForSubtypes(ModItems.CHARGING_BATTERY);
    registration.useNbtForSubtypes(ModItems.ADVANCED_CHARGING_BATTERY);
    registration.useNbtForSubtypes(ModItems.CHARGING_ENERGY_CRYSTAL);
    registration.useNbtForSubtypes(ModItems.CHARGING_LAPOTRON_CRYSTAL);
  }
}
