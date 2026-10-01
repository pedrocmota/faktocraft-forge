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
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.BlockEntityUraniumCentrifuge;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.MenuUraniumCentrifuge;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.ScreenUraniumCentrifuge;
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
import com.faktocraft.integration.jei.category.impl.UraniumCentrifugingCategory;
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
import com.faktocraft.common.util.RecipeUtil;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final Identifier UID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, Faktocraft.MODID);

  @Override
  public Identifier getPluginUid() {
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
    registration.addRecipeCategories(new UraniumCentrifugingCategory(guiHelper));
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

    registration.addRecipes(CrushingCategory.TYPE, recipes(level, ModRecipeType.CRUSHING));
    registration.addRecipes(CompressingCategory.TYPE, recipes(level, ModRecipeType.COMPRESSING));
    registration.addRecipes(ExtractingCategory.TYPE, recipes(level, ModRecipeType.EXTRACTING));
    registration.addRecipes(FluidExtrudingCategory.TYPE, recipes(level, ModRecipeType.FLUID_EXTRUDING));
    registration.addRecipes(SawingCategory.TYPE, recipes(level, ModRecipeType.SAWING));
    registration.addRecipes(AlloySmeltingCategory.TYPE, recipes(level, ModRecipeType.ALLOY_SMELTING));
    registration.addRecipes(CircuitAssemblingCategory.TYPE, recipes(level, ModRecipeType.CIRCUIT_ASSEMBLING));
    registration.addRecipes(RecyclingCategory.TYPE, recipes(level, ModRecipeType.RECYCLING));
    registration.addRecipes(FluidEnrichingCategory.TYPE, recipes(level, ModRecipeType.FLUID_ENRICHING));
    registration.addRecipes(OreWashingCategory.TYPE, recipes(level, ModRecipeType.ORE_WASHING));
    registration.addRecipes(PolymerizingCategory.TYPE, recipes(level, ModRecipeType.POLYMERIZING));
    registration.addRecipes(ThermalCentrifugingCategory.TYPE, recipes(level, ModRecipeType.THERMAL_CENTRIFUGING));
    registration.addRecipes(UraniumCentrifugingCategory.TYPE, recipes(level, ModRecipeType.URANIUM_CENTRIFUGING));
    registration.addRecipes(ScannerCategory.TYPE, recipes(level, ModRecipeType.SCANNER));
    registration.addRecipes(RollingCategory.TYPE, recipes(level, ModRecipeType.ROLLING));
    registration.addRecipes(CuttingCategory.TYPE, recipes(level, ModRecipeType.CUTTING));
    registration.addRecipes(ExtrudingCategory.TYPE, recipes(level, ModRecipeType.EXTRUDING));

    registration.addRecipes(FermentingCategory.TYPE, List.of(fermentingEntry()));
    registration.addRecipes(DistillingCategory.TYPE, List.of(distillingEntry()));
    registration.addRecipes(MatterFabricatingCategory.TYPE, matterFabricatingEntries());

    registration.addIngredientInfo(ModItems.FERTILIZER,
        net.minecraft.network.chat.Component.translatable("jei." + Faktocraft.MODID + ".fertilizer.info",
            BlockEntityFermenter.WASTE_EVERY_TICKS / 20));

    List<ScrapBoxRecipe> scrapBoxRecipes = recipes(level, ModRecipeType.SCRAP_BOX);
    ScrapBoxCategory.setTotalWeight(ScrapBoxRecipe.getTotalWeight(scrapBoxRecipes));
    registration.addRecipes(ScrapBoxCategory.TYPE, scrapBoxRecipes);

    Faktocraft.LOGGER.info("Loaded JEI recipe integration in {} ms", (System.nanoTime() - start) / 1_000_000);
  }

  private static FermentingCategory.Entry fermentingEntry() {
    return new FermentingCategory.Entry(
        new net.minecraft.world.item.ItemStack(ModItems.MUD_PILE, BlockEntityFermenter.MUD_PER_OP),
        new net.neoforged.neoforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.BIOMASS.still(),
            BlockEntityFermenter.BIOMASS_PER_OP),
        new net.neoforged.neoforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.BIOGAS.still(),
            BlockEntityFermenter.BIOGAS_PER_OP),
        BlockEntityFermenter.DURATION_TICKS,
        com.faktocraft.common.config.ModConfig.server().fermenter_tick_usage);
  }

  private static DistillingCategory.Entry distillingEntry() {
    return new DistillingCategory.Entry(
        new net.neoforged.neoforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.OIL.still(),
            BlockEntityDistillery.OIL_PER_OP),
        new net.neoforged.neoforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.SULFURIC_ACID.still(),
            BlockEntityDistillery.ACID_PER_OP),
        new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,
            BlockEntityDistillery.WATER_PER_OP),
        new net.neoforged.neoforge.fluids.FluidStack(com.faktocraft.common.fluid.ModFluids.FUEL.still(),
            BlockEntityDistillery.FUEL_PER_OP),
        new net.minecraft.world.item.ItemStack(ModItems.SULFUR_DUST),
        BlockEntityDistillery.SULFUR_CHANCE,
        BlockEntityDistillery.DURATION_TICKS,
        BlockEntityDistillery.POWER_PER_TICK);
  }

  private static List<MatterFabricatingCategory.Entry> matterFabricatingEntries() {
    net.neoforged.neoforge.fluids.FluidStack matter = new net.neoforged.neoforge.fluids.FluidStack(
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

  private static <I extends RecipeInput, T extends Recipe<I>> List<T> recipes(Level level, RecipeType<T> type) {
    return RecipeUtil.getAllRecipesFor(level, type);
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addCraftingStation(RecipeTypes.SMELTING, M2Registry.IRON_FURNACE, M2Registry.ELECTRIC_FURNACE);

    registration.addCraftingStation(CrushingCategory.TYPE, M2Registry.CRUSHER);
    registration.addCraftingStation(CompressingCategory.TYPE, M2Registry.COMPRESSOR);
    registration.addCraftingStation(ExtractingCategory.TYPE, M2Registry.EXTRACTOR);
    registration.addCraftingStation(FluidExtrudingCategory.TYPE, M3Registry.EXTRUDER);
    registration.addCraftingStation(SawingCategory.TYPE, M2Registry.SAWMILL);
    registration.addCraftingStation(AlloySmeltingCategory.TYPE, M3Registry.ALLOY_SMELTER,
        M3Registry.COAL_ALLOY_SMELTER, M3Registry.COMBUSTION_ALLOY_SMELTER);
    registration.addCraftingStation(CircuitAssemblingCategory.TYPE, M3Registry.CIRCUIT_ASSEMBLER);
    registration.addCraftingStation(RecyclingCategory.TYPE, M2Registry.RECYCLER);
    registration.addCraftingStation(FluidEnrichingCategory.TYPE, M3Registry.FLUID_ENRICHER);
    registration.addCraftingStation(OreWashingCategory.TYPE, M3Registry.ORE_WASHING_PLANT);
    registration.addCraftingStation(PolymerizingCategory.TYPE, M3Registry.POLYMERIZER);
    registration.addCraftingStation(ThermalCentrifugingCategory.TYPE, M3Registry.THERMAL_CENTRIFUGE);
    registration.addCraftingStation(UraniumCentrifugingCategory.TYPE, M3Registry.URANIUM_CENTRIFUGE);
    registration.addCraftingStation(RollingCategory.TYPE, M3Registry.METAL_FORMER);
    registration.addCraftingStation(CuttingCategory.TYPE, M3Registry.METAL_FORMER);
    registration.addCraftingStation(ExtrudingCategory.TYPE, M3Registry.METAL_FORMER);
    registration.addCraftingStation(FermentingCategory.TYPE, M3Registry.FERMENTER);
    registration.addCraftingStation(DistillingCategory.TYPE,
        com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry.DISTILLERY);
    registration.addCraftingStation(MatterFabricatingCategory.TYPE, M4Registry.MATTER_FABRICATOR);
    registration.addCraftingStation(RecipeTypes.CRAFTING,
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
        ClientLevel level = Minecraft.getInstance().level;
        CraftingRecipe recipe = current.get();
        ItemStack result = recipe != null && level != null ? resultOf(recipe, level) : ItemStack.EMPTY;
        if (recipe == null || level == null || activeRuntime == null || result.isEmpty()) {
          recipesGui.showTypes(List.of(RecipeTypes.CRAFTING));
          return;
        }
        List<mezz.jei.api.recipe.IFocus<?>> focuses = List.of(focusFactory.createFocus(
            mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT,
            mezz.jei.api.constants.VanillaTypes.ITEM_STACK, result));
        mezz.jei.api.recipe.IRecipeManager manager = activeRuntime.getRecipeManager();

        Optional<Identifier> id = RecipeUtil.idOf(level, recipe);
        java.util.function.Predicate<RecipeHolder<CraftingRecipe>> same = other -> other.value() == recipe
            || (id.isPresent() && other.id().identifier().equals(id.get()));
        List<RecipeHolder<CraftingRecipe>> found = manager.createRecipeLookup(RecipeTypes.CRAFTING)
            .limitFocus(focuses).get().toList();
        List<RecipeHolder<CraftingRecipe>> ordered = new java.util.ArrayList<>();
        found.stream().filter(same).forEach(ordered::add);
        found.stream().filter(same.negate()).forEach(ordered::add);
        if (ordered.isEmpty()) {
          recipesGui.show(focuses);
          return;
        }
        recipesGui.showRecipes(manager.getRecipeCategory(RecipeTypes.CRAFTING), ordered, focuses);
      }
    };
  }

  private static ItemStack resultOf(CraftingRecipe recipe, Level level) {
    net.minecraft.util.context.ContextMap context = SlotDisplayContext.fromLevel(level);
    for (RecipeDisplay display : recipe.display()) {
      ItemStack stack = display.result().resolveForFirstStack(context);
      if (!stack.isEmpty()) {
        return stack;
      }
    }
    return ItemStack.EMPTY;
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
    registration.addRecipeClickArea(
        com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenCoalAlloySmelter.class, 82, 33, 24, 16,
        AlloySmeltingCategory.TYPE);
    registration.addRecipeClickArea(
        com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenCombustionAlloySmelter.class, 82, 33, 24, 16,
        AlloySmeltingCategory.TYPE);
    registration.addRecipeClickArea(ScreenCircuitAssembler.class, 81, 33, 24, 16, CircuitAssemblingCategory.TYPE);
    registration.addRecipeClickArea(ScreenRecycler.class, 71, 35, 24, 16, RecyclingCategory.TYPE);
    registration.addRecipeClickArea(ScreenFluidEnricher.class, 76, 35, 24, 16, FluidEnrichingCategory.TYPE);
    registration.addRecipeClickArea(ScreenOreWashingPlant.class, 90, 32, 19, 19, OreWashingCategory.TYPE);
    registration.addRecipeClickArea(ScreenPolymerizer.class, 88, 35, 24, 16, PolymerizingCategory.TYPE);
    registration.addRecipeClickArea(ScreenThermalCentrifuge.class, 82, 33, 24, 16, ThermalCentrifugingCategory.TYPE);
    registration.addRecipeClickArea(ScreenUraniumCentrifuge.class, 71, 35, 24, 16, UraniumCentrifugingCategory.TYPE);
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
    registration.addGuiContainerHandler(com.faktocraft.common.block.impl.forester.ScreenForester.class,
        new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
          @Override
          public List<net.minecraft.client.renderer.Rect2i> getGuiExtraAreas(
              com.faktocraft.common.block.impl.forester.ScreenForester screen) {
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
              role = mezz.jei.api.recipe.RecipeIngredientRole.CRAFTING_STATION;
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
            return List.of(typesArea(66, 52, 28, 23, RecipeTypes.CRAFTING));
          }
        });
  }

  private static mezz.jei.api.gui.handlers.IGuiClickableArea typesArea(int x, int y, int width, int height,
      mezz.jei.api.recipe.types.IRecipeType<?>... types) {
    net.minecraft.client.renderer.Rect2i area = new net.minecraft.client.renderer.Rect2i(x, y, width, height);
    List<mezz.jei.api.recipe.types.IRecipeType<?>> shown = List.of(types);
    return new mezz.jei.api.gui.handlers.IGuiClickableArea() {
      @Override
      public net.minecraft.client.renderer.Rect2i getArea() {
        return area;
      }

      @Override
      public void onClick(mezz.jei.api.recipe.IFocusFactory focusFactory,
          mezz.jei.api.runtime.IRecipesGui recipesGui) {
        recipesGui.showTypes(shown);
      }
    };
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
        com.faktocraft.common.block.impl.logistics.MenuRequestTable.STORAGE_START,
        com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable.STORAGE_SLOTS + 36);
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuCrusher.class, M2Registry.CRUSHER_MENU,
        CrushingCategory.TYPE, BlockEntityCrusher.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuCompressor.class,
        M2Registry.COMPRESSOR_MENU, CompressingCategory.TYPE, BlockEntityCompressor.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuExtractor.class, M2Registry.EXTRACTOR_MENU,
        ExtractingCategory.TYPE, BlockEntityExtractor.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuSawmill.class, M2Registry.SAWMILL_MENU,
        SawingCategory.TYPE, BlockEntitySawmill.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuAlloySmelter.class,
        M3Registry.ALLOY_SMELTER_MENU, AlloySmeltingCategory.TYPE, BlockEntityAlloySmelter.INPUT_SLOT_0, 3));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(
        com.faktocraft.common.block.impl.machines.alloy_smelter.MenuCoalAlloySmelter.class,
        M3Registry.COAL_ALLOY_SMELTER_MENU, AlloySmeltingCategory.TYPE, BlockEntityAlloySmelter.INPUT_SLOT_0, 3));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(
        com.faktocraft.common.block.impl.machines.alloy_smelter.MenuCombustionAlloySmelter.class,
        M3Registry.COMBUSTION_ALLOY_SMELTER_MENU, AlloySmeltingCategory.TYPE, BlockEntityAlloySmelter.INPUT_SLOT_0,
        3));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuCircuitAssembler.class,
        M3Registry.CIRCUIT_ASSEMBLER_MENU, CircuitAssemblingCategory.TYPE, BlockEntityCircuitAssembler.INPUT_SLOT_0,
        3));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuRecycler.class, M2Registry.RECYCLER_MENU,
        RecyclingCategory.TYPE, BlockEntityRecycler.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuFluidEnricher.class,
        M3Registry.FLUID_ENRICHER_MENU, FluidEnrichingCategory.TYPE, BlockEntityFluidEnricher.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuOreWashingPlant.class,
        M3Registry.ORE_WASHING_PLANT_MENU, OreWashingCategory.TYPE, BlockEntityOreWashingPlant.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuPolymerizer.class,
        M3Registry.POLYMERIZER_MENU, PolymerizingCategory.TYPE, BlockEntityPolymerizer.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuThermalCentrifuge.class,
        M3Registry.THERMAL_CENTRIFUGE_MENU, ThermalCentrifugingCategory.TYPE, BlockEntityThermalCentrifuge.INPUT_SLOT,
        1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuUraniumCentrifuge.class,
        M3Registry.URANIUM_CENTRIFUGE_MENU, UraniumCentrifugingCategory.TYPE,
        BlockEntityUraniumCentrifuge.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuScanner.class, M4Registry.SCANNER_MENU,
        ScannerCategory.TYPE, BlockEntityScanner.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuMetalFormer.class,
        M3Registry.METAL_FORMER_MENU, RollingCategory.TYPE, BlockEntityMetalFormer.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuMetalFormer.class,
        M3Registry.METAL_FORMER_MENU, CuttingCategory.TYPE, BlockEntityMetalFormer.INPUT_SLOT, 1));
    registration.addRecipeTransferHandler(new MachineTransferInfo<>(MenuMetalFormer.class,
        M3Registry.METAL_FORMER_MENU, ExtrudingCategory.TYPE, BlockEntityMetalFormer.INPUT_SLOT, 1));
  }

  private static final ISubtypeInterpreter<ItemStack> ALL_COMPONENTS = (stack, context) -> {
    DataComponentPatch patch = stack.getComponentsPatch();
    return patch.isEmpty() ? null : patch;
  };

  private static void useNbtForSubtypes(ISubtypeRegistration registration, Item item) {
    registration.registerSubtypeInterpreter(item, ALL_COMPONENTS);
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    useNbtForSubtypes(registration, ModItems.NANO_SABER);
    useNbtForSubtypes(registration, ModItems.NANO_HELMET);
    useNbtForSubtypes(registration, ModItems.NANO_CHESTPLATE);
    useNbtForSubtypes(registration, ModItems.NANO_LEGGINGS);
    useNbtForSubtypes(registration, ModItems.NANO_BOOTS);

    useNbtForSubtypes(registration, ModItems.FLUID_CELL);
    useNbtForSubtypes(registration, ModItems.MEDIUM_COOLANT_CELL);
    useNbtForSubtypes(registration, ModItems.LARGE_COOLANT_CELL);

    useNbtForSubtypes(registration, ModItems.ELECTRIC_HOE);
    useNbtForSubtypes(registration, ModItems.ELECTRIC_WRENCH);
    useNbtForSubtypes(registration, ModItems.ELECTRIC_TREETAP);
    useNbtForSubtypes(registration, ModItems.MULTI_TOOL);

    useNbtForSubtypes(registration, ModItems.MINING_DRILL);
    useNbtForSubtypes(registration, ModItems.DIAMOND_DRILL);
    useNbtForSubtypes(registration, ModItems.IRIDIUM_DRILL);

    useNbtForSubtypes(registration, ModItems.CHAINSAW);
    useNbtForSubtypes(registration, ModItems.DIAMOND_CHAINSAW);
    useNbtForSubtypes(registration, ModItems.IRIDIUM_CHAINSAW);

    useNbtForSubtypes(registration, ModItems.BATTERY);
    useNbtForSubtypes(registration, ModItems.ADVANCED_BATTERY);
    useNbtForSubtypes(registration, ModItems.MEDIUM_BATTERY);
    useNbtForSubtypes(registration, ModItems.ADVANCED_MEDIUM_BATTERY);
    useNbtForSubtypes(registration, ModItems.ENERGY_CRYSTAL);
    useNbtForSubtypes(registration, ModItems.LAPOTRON_CRYSTAL);
    useNbtForSubtypes(registration, ModItems.ADVANCED_ENERGY_CRYSTAL);
    useNbtForSubtypes(registration, ModItems.ADVANCED_LAPOTRON_CRYSTAL);
    useNbtForSubtypes(registration, ModItems.IRIDIUM_CRYSTAL);

    useNbtForSubtypes(registration, ModItems.CHARGING_BATTERY);
    useNbtForSubtypes(registration, ModItems.ADVANCED_CHARGING_BATTERY);
    useNbtForSubtypes(registration, ModItems.CHARGING_ENERGY_CRYSTAL);
    useNbtForSubtypes(registration, ModItems.CHARGING_LAPOTRON_CRYSTAL);
  }
}
