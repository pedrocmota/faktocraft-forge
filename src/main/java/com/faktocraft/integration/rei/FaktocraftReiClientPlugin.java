package com.faktocraft.integration.rei;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.forester.ScreenForester;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.MenuAssemblyTable;
import com.faktocraft.common.block.impl.logistics.ScreenAssemblyTable;
import com.faktocraft.common.block.impl.logistics.ScreenChassis;
import com.faktocraft.common.block.impl.logistics.ScreenCraftPipe;
import com.faktocraft.common.block.impl.logistics.ScreenRecipePipe;
import com.faktocraft.common.block.impl.logistics.ScreenRequestTable;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.MenuRequestTable;
import com.faktocraft.common.block.impl.machines.alloy_smelter.BlockEntityAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.MenuAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.MenuCoalAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.MenuCombustionAlloySmelter;
import com.faktocraft.common.block.impl.machines.circuit_assembler.BlockEntityCircuitAssembler;
import com.faktocraft.common.block.impl.machines.circuit_assembler.MenuCircuitAssembler;
import com.faktocraft.common.block.impl.machines.compressor.BlockEntityCompressor;
import com.faktocraft.common.block.impl.machines.compressor.MenuCompressor;
import com.faktocraft.common.block.impl.machines.crusher.BlockEntityCrusher;
import com.faktocraft.common.block.impl.machines.crusher.MenuCrusher;
import com.faktocraft.common.block.impl.machines.extractor.BlockEntityExtractor;
import com.faktocraft.common.block.impl.machines.extractor.MenuExtractor;
import com.faktocraft.common.block.impl.machines.fluid_enricher.BlockEntityFluidEnricher;
import com.faktocraft.common.block.impl.machines.fluid_enricher.MenuFluidEnricher;
import com.faktocraft.common.block.impl.machines.metal_former.BlockEntityMetalFormer;
import com.faktocraft.common.block.impl.machines.metal_former.MenuMetalFormer;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.BlockEntityOreWashingPlant;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.MenuOreWashingPlant;
import com.faktocraft.common.block.impl.machines.polymerizer.BlockEntityPolymerizer;
import com.faktocraft.common.block.impl.machines.polymerizer.MenuPolymerizer;
import com.faktocraft.common.block.impl.machines.recycler.BlockEntityRecycler;
import com.faktocraft.common.block.impl.machines.recycler.MenuRecycler;
import com.faktocraft.common.block.impl.machines.sawmill.BlockEntitySawmill;
import com.faktocraft.common.block.impl.machines.sawmill.MenuSawmill;
import com.faktocraft.common.block.impl.machines.scanner.BlockEntityScanner;
import com.faktocraft.common.block.impl.machines.scanner.MenuScanner;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.BlockEntityThermalCentrifuge;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.MenuThermalCentrifuge;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.BlockEntityUraniumCentrifuge;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.MenuUraniumCentrifuge;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.ScreenUraniumCentrifuge;
import com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenCoalAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenCombustionAlloySmelter;
import com.faktocraft.common.block.impl.machines.circuit_assembler.ScreenCircuitAssembler;
import com.faktocraft.common.block.impl.machines.compressor.ScreenCompressor;
import com.faktocraft.common.block.impl.machines.crusher.ScreenCrusher;
import com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry;
import com.faktocraft.common.block.impl.machines.distillery.ScreenDistillery;
import com.faktocraft.common.block.impl.machines.electric_furnace.ScreenElectricFurnace;
import com.faktocraft.common.block.impl.machines.extractor.ScreenExtractor;
import com.faktocraft.common.block.impl.machines.extruder.ScreenExtruder;
import com.faktocraft.common.block.impl.machines.fermenter.BlockEntityFermenter;
import com.faktocraft.common.block.impl.machines.fermenter.ScreenFermenter;
import com.faktocraft.common.block.impl.machines.fluid_enricher.ScreenFluidEnricher;
import com.faktocraft.common.block.impl.machines.iron_furnace.ScreenIronFurnace;
import com.faktocraft.common.block.impl.machines.matter_fabricator.ScreenMatterFabricator;
import com.faktocraft.common.block.impl.machines.metal_former.ScreenMetalFormer;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.ScreenOreWashingPlant;
import com.faktocraft.common.block.impl.machines.polymerizer.ScreenPolymerizer;
import com.faktocraft.common.block.impl.machines.recycler.ScreenRecycler;
import com.faktocraft.common.block.impl.machines.sawmill.ScreenSawmill;
import com.faktocraft.common.block.impl.machines.scanner.ScreenScanner;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.ScreenThermalCentrifuge;
import com.faktocraft.common.block.impl.pipe.ScreenExtractorPipe;
import com.faktocraft.common.block.impl.quarry.ScreenQuarry;
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
import com.faktocraft.common.registries.ModCreativeTab;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.ModRecipeType;
import com.faktocraft.common.registries.machines.M2Registry;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.screen.PanelScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@REIPluginClient
public class FaktocraftReiClientPlugin implements REIClientPlugin {

  private static final int ARROW_W = 24;
  private static final int ARROW_H = 16;

  @Override
  public void registerCategories(CategoryRegistry registry) {
    for (MachineCategory category : ReiCategories.all()) {
      registry.add(category);
    }
    registry.addWorkstations(BuiltinPlugin.SMELTING, EntryStacks.of(M2Registry.IRON_FURNACE),
        EntryStacks.of(M2Registry.ELECTRIC_FURNACE));
    registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(LogisticsRegistry.REQUEST_TABLE_ITEM),
        EntryStacks.of(LogisticsRegistry.ASSEMBLY_TABLE_ITEM));
    registry.addWorkstations(ReiCategories.CRUSHING, EntryStacks.of(M2Registry.CRUSHER));
    registry.addWorkstations(ReiCategories.COMPRESSING, EntryStacks.of(M2Registry.COMPRESSOR));
    registry.addWorkstations(ReiCategories.EXTRACTING, EntryStacks.of(M2Registry.EXTRACTOR));
    registry.addWorkstations(ReiCategories.FLUID_EXTRUDING, EntryStacks.of(M3Registry.EXTRUDER));
    registry.addWorkstations(ReiCategories.SAWING, EntryStacks.of(M2Registry.SAWMILL));
    registry.addWorkstations(ReiCategories.ALLOY_SMELTING, EntryStacks.of(M3Registry.ALLOY_SMELTER),
        EntryStacks.of(M3Registry.COAL_ALLOY_SMELTER), EntryStacks.of(M3Registry.COMBUSTION_ALLOY_SMELTER));
    registry.addWorkstations(ReiCategories.CIRCUIT_ASSEMBLING, EntryStacks.of(M3Registry.CIRCUIT_ASSEMBLER));
    registry.addWorkstations(ReiCategories.RECYCLING, EntryStacks.of(M2Registry.RECYCLER));
    registry.addWorkstations(ReiCategories.FLUID_ENRICHING, EntryStacks.of(M3Registry.FLUID_ENRICHER));
    registry.addWorkstations(ReiCategories.ORE_WASHING, EntryStacks.of(M3Registry.ORE_WASHING_PLANT));
    registry.addWorkstations(ReiCategories.POLYMERIZING, EntryStacks.of(M3Registry.POLYMERIZER));
    registry.addWorkstations(ReiCategories.THERMAL_CENTRIFUGING, EntryStacks.of(M3Registry.THERMAL_CENTRIFUGE));
    registry.addWorkstations(ReiCategories.URANIUM_CENTRIFUGING, EntryStacks.of(M3Registry.URANIUM_CENTRIFUGE));
    registry.addWorkstations(ReiCategories.SCANNER, EntryStacks.of(M4Registry.SCANNER));
    registry.addWorkstations(ReiCategories.SCRAP_BOX, EntryStacks.of(ModItems.SCRAP_BOX));
    registry.addWorkstations(ReiCategories.ROLLING, EntryStacks.of(M3Registry.METAL_FORMER));
    registry.addWorkstations(ReiCategories.CUTTING, EntryStacks.of(M3Registry.METAL_FORMER));
    registry.addWorkstations(ReiCategories.EXTRUDING, EntryStacks.of(M3Registry.METAL_FORMER));
    registry.addWorkstations(ReiCategories.FERMENTING, EntryStacks.of(M3Registry.FERMENTER));
    registry.addWorkstations(ReiCategories.DISTILLING, EntryStacks.of(DistilleryRegistry.DISTILLERY));
    registry.addWorkstations(ReiCategories.MATTER_FABRICATING, EntryStacks.of(M4Registry.MATTER_FABRICATOR));
  }

  @Override
  public void registerDisplays(DisplayRegistry registry) {
    registry.registerRecipeFiller(CrushingRecipe.class, ModRecipeType.CRUSHING, ReiDisplays::crushing);
    registry.registerRecipeFiller(CompressingRecipe.class, ModRecipeType.COMPRESSING, ReiDisplays::compressing);
    registry.registerRecipeFiller(ExtractingRecipe.class, ModRecipeType.EXTRACTING, ReiDisplays::extracting);
    registry.registerRecipeFiller(FluidExtrudingRecipe.class, ModRecipeType.FLUID_EXTRUDING,
        ReiDisplays::fluidExtruding);
    registry.registerRecipeFiller(SawingRecipe.class, ModRecipeType.SAWING, ReiDisplays::sawing);
    registry.registerRecipeFiller(AlloySmeltingRecipe.class, ModRecipeType.ALLOY_SMELTING,
        ReiDisplays::alloySmelting);
    registry.registerRecipeFiller(CircuitAssemblingRecipe.class, ModRecipeType.CIRCUIT_ASSEMBLING,
        ReiDisplays::circuitAssembling);
    registry.registerRecipeFiller(RecyclingRecipe.class, ModRecipeType.RECYCLING, ReiDisplays::recycling);
    registry.registerRecipeFiller(FluidEnrichingRecipe.class, ModRecipeType.FLUID_ENRICHING,
        ReiDisplays::fluidEnriching);
    registry.registerRecipeFiller(OreWashingRecipe.class, ModRecipeType.ORE_WASHING, ReiDisplays::oreWashing);
    registry.registerRecipeFiller(PolymerizingRecipe.class, ModRecipeType.POLYMERIZING, ReiDisplays::polymerizing);
    registry.registerRecipeFiller(ThermalCentrifugingRecipe.class, ModRecipeType.THERMAL_CENTRIFUGING,
        ReiDisplays::thermalCentrifuging);
    registry.registerRecipeFiller(UraniumCentrifugingRecipe.class, ModRecipeType.URANIUM_CENTRIFUGING,
        ReiDisplays::uraniumCentrifuging);
    registry.registerRecipeFiller(ScannerRecipe.class, ModRecipeType.SCANNER, ReiDisplays::scanner);
    registry.registerRecipeFiller(RollingRecipe.class, ModRecipeType.ROLLING, ReiDisplays::rolling);
    registry.registerRecipeFiller(CuttingRecipe.class, ModRecipeType.CUTTING, ReiDisplays::cutting);
    registry.registerRecipeFiller(ExtrudingRecipe.class, ModRecipeType.EXTRUDING, ReiDisplays::extruding);

    List<ScrapBoxRecipe> scrapBoxRecipes = registry.getRecipeManager().getAllRecipesFor(ModRecipeType.SCRAP_BOX);
    float totalWeight = ScrapBoxRecipe.getTotalWeight(scrapBoxRecipes);
    registry.registerRecipeFiller(ScrapBoxRecipe.class, ModRecipeType.SCRAP_BOX,
        recipe -> ReiDisplays.scrapBox(recipe, totalWeight));

    registry.add(ReiDisplays.fermenting());
    registry.add(ReiDisplays.distilling());
    for (MachineDisplay display : ReiDisplays.matterFabricating()) {
      registry.add(display);
    }

    BuiltinClientPlugin.getInstance().registerInformation(EntryStacks.of(ModItems.FERTILIZER),
        ModItems.FERTILIZER.getDescription(), lines -> {
          lines.add(Component.translatable("jei." + Faktocraft.MODID + ".fertilizer.info",
              BlockEntityFermenter.WASTE_EVERY_TICKS / 20));
          return lines;
        });
  }

  @Override
  public void registerEntries(EntryRegistry registry) {
    Predicate<EntryStack<?>> unreleased = stack -> stack.getType() == VanillaEntryTypes.ITEM
        && ModCreativeTab.isUnreleased(((ItemStack) stack.getValue()).getItem());
    registry.removeEntryIf(unreleased);
  }

  private static <C extends AbstractContainerMenu, T extends AbstractContainerScreen<C>> void area(
      ScreenRegistry registry, Class<? extends T> screen, int x, int y, int w, int h,
      CategoryIdentifier<?>... categories) {
    registry.<C, T>registerContainerClickArea(new Rectangle(x, y, w, h), screen, categories);
  }

  private static ClickArea.Result openRecipe(Supplier<CraftingRecipe> current) {
    return ClickArea.Result.success().category(BuiltinPlugin.CRAFTING).executor(() -> {
      CraftingRecipe recipe = current.get();
      var level = Minecraft.getInstance().level;
      if (recipe == null || level == null) {
        return false;
      }
      return ViewSearchBuilder.builder().addRecipesFor(EntryStacks.of(recipe.getResultItem(level.registryAccess())))
          .filterCategory(BuiltinPlugin.CRAFTING).open();
    });
  }

  private static ClickArea.Result openUsages(ItemStack stack, boolean recipes) {
    ViewSearchBuilder builder = ViewSearchBuilder.builder();
    EntryStack<ItemStack> entry = EntryStacks.of(stack);
    if (recipes) {
      builder.addRecipesFor(entry);
    } else {
      builder.addUsagesFor(entry);
    }
    return ClickArea.Result.success().executor(builder::open);
  }

  private static boolean hovering(ClickArea.ClickAreaContext<? extends AbstractContainerScreen<?>> context, int x,
      int y, int w, int h) {
    AbstractContainerScreen<?> screen = context.getScreen();
    Rectangle rect = new Rectangle(screen.getGuiLeft() + x, screen.getGuiTop() + y, w, h);
    return rect.contains(context.getMousePosition());
  }

  @Override
  public void registerScreens(ScreenRegistry registry) {
    area(registry, ScreenIronFurnace.class, 80, 35, ARROW_W, ARROW_H, BuiltinPlugin.SMELTING);
    area(registry, ScreenElectricFurnace.class, 71, 35, ARROW_W, ARROW_H, BuiltinPlugin.SMELTING);
    area(registry, ScreenCrusher.class, 71, 35, ARROW_W, ARROW_H, ReiCategories.CRUSHING);
    area(registry, ScreenCompressor.class, 71, 35, ARROW_W, ARROW_H, ReiCategories.COMPRESSING);
    area(registry, ScreenExtractor.class, 71, 35, ARROW_W, ARROW_H, ReiCategories.EXTRACTING);
    area(registry, ScreenExtruder.class, 78, 35, ARROW_W, ARROW_H, ReiCategories.FLUID_EXTRUDING);
    area(registry, ScreenSawmill.class, 71, 35, ARROW_W, ARROW_H, ReiCategories.SAWING);
    area(registry, ScreenAlloySmelter.class, 82, 33, ARROW_W, ARROW_H, ReiCategories.ALLOY_SMELTING);
    area(registry, ScreenCoalAlloySmelter.class, 82, 33, ARROW_W, ARROW_H, ReiCategories.ALLOY_SMELTING);
    area(registry, ScreenCombustionAlloySmelter.class, 82, 33, ARROW_W, ARROW_H, ReiCategories.ALLOY_SMELTING);
    area(registry, ScreenCircuitAssembler.class, 81, 33, ARROW_W, ARROW_H, ReiCategories.CIRCUIT_ASSEMBLING);
    area(registry, ScreenRecycler.class, 71, 35, ARROW_W, ARROW_H, ReiCategories.RECYCLING);
    area(registry, ScreenFluidEnricher.class, 76, 35, ARROW_W, ARROW_H, ReiCategories.FLUID_ENRICHING);
    area(registry, ScreenOreWashingPlant.class, 90, 32, 19, 19, ReiCategories.ORE_WASHING);
    area(registry, ScreenPolymerizer.class, 88, 35, ARROW_W, ARROW_H, ReiCategories.POLYMERIZING);
    area(registry, ScreenThermalCentrifuge.class, 82, 33, ARROW_W, ARROW_H, ReiCategories.THERMAL_CENTRIFUGING);
    area(registry, ScreenUraniumCentrifuge.class, 71, 35, ARROW_W, ARROW_H, ReiCategories.URANIUM_CENTRIFUGING);
    area(registry, ScreenFermenter.class, 76, 35, ARROW_W, ARROW_H, ReiCategories.FERMENTING);
    area(registry, ScreenDistillery.class, 94, 43, ARROW_W, ARROW_H, ReiCategories.DISTILLING);
    area(registry, ScreenMatterFabricator.class, 104, 51, ARROW_W, ARROW_H, ReiCategories.MATTER_FABRICATING);
    area(registry, ScreenScanner.class, 7, 15, 61, 9, ReiCategories.SCANNER);
    area(registry, ScreenScanner.class, 7, 24, 18, 26, ReiCategories.SCANNER);
    area(registry, ScreenScanner.class, 51, 24, 18, 26, ReiCategories.SCANNER);
    area(registry, ScreenScanner.class, 7, 50, 61, 9, ReiCategories.SCANNER);
    area(registry, ScreenMetalFormer.class, 71, 34, 24, 18, ReiCategories.ROLLING, ReiCategories.CUTTING,
        ReiCategories.EXTRUDING);

    registry.registerClickArea(ScreenAssemblyTable.class, context -> {
      ScreenAssemblyTable screen = context.getScreen();
      if (!screen.showsRecipe()
          || !hovering(context, MenuAssemblyTable.ARROW_X, MenuAssemblyTable.ARROW_Y, ARROW_W, ARROW_H)) {
        return ClickArea.Result.fail();
      }
      return openRecipe(screen::currentRecipe);
    });
    registry.registerClickArea(ScreenCraftPipe.class, context -> {
      ScreenCraftPipe screen = context.getScreen();
      if (!screen.isEditing() || !hovering(context, ScreenCraftPipe.ARROW_X, ScreenCraftPipe.ARROW_Y, ARROW_W,
          ARROW_H)) {
        return ClickArea.Result.fail();
      }
      return openRecipe(screen::currentRecipe);
    });
    registry.registerClickArea(ScreenRecipePipe.class, context -> {
      ScreenRecipePipe screen = context.getScreen();
      if (!screen.isEditing() || !hovering(context, ScreenRecipePipe.ARROW_X, screen.arrowY(), ARROW_W, ARROW_H)) {
        return ClickArea.Result.fail();
      }
      ItemStack stack = screen.declaredOutput();
      boolean recipes = true;
      if (stack.isEmpty()) {
        stack = screen.dockedStack();
        recipes = false;
      }
      if (stack.isEmpty()) {
        stack = screen.declaredInput();
        recipes = false;
      }
      return stack.isEmpty() ? ClickArea.Result.fail() : openUsages(stack, recipes);
    });
    registry.registerClickArea(ScreenRequestTable.class, context -> {
      ScreenRequestTable screen = context.getScreen();
      if (!screen.isRequestsTab() || !hovering(context, 66, 52, 28, 23)) {
        return ClickArea.Result.fail();
      }
      return ClickArea.Result.success().category(BuiltinPlugin.CRAFTING);
    });
  }

  private static List<Rectangle> rectangles(List<Rect2i> areas) {
    return areas.stream().map(area -> new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()))
        .toList();
  }

  private static <T extends Screen> void zone(ExclusionZones zones, Class<T> screen, Function<T, List<Rect2i>> areas) {
    zones.register(screen, s -> rectangles(areas.apply(s)));
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void registerExclusionZones(ExclusionZones zones) {
    zone(zones, (Class<PanelScreen>) (Class) PanelScreen.class, PanelScreen::getAreas);
    zone(zones, ScreenQuarry.class, ScreenQuarry::extraAreas);
    zone(zones, ScreenForester.class, ScreenForester::extraAreas);
    zone(zones, ScreenChassis.class, ScreenChassis::extraAreas);
    zone(zones, ScreenExtractorPipe.class, ScreenExtractorPipe::extraAreas);
  }

  @Override
  public void registerTransferHandlers(TransferHandlerRegistry registry) {
    registry.register(PipeTransfers.CRAFT_PIPE_PATTERN);
    registry.register(PipeTransfers.RECIPE_PIPE);
    registry.register(new MachineTransfer(MenuRequestTable.class, BuiltinPlugin.CRAFTING,
        MenuRequestTable.MATRIX_START, 9, MenuRequestTable.STORAGE_START,
        BlockEntityRequestTable.STORAGE_SLOTS + MachineTransfer.PLAYER_SLOTS));
    registry.register(MachineTransfer.of(MenuCrusher.class, ReiCategories.CRUSHING, BlockEntityCrusher.INPUT_SLOT, 1,
        1));
    registry.register(MachineTransfer.of(MenuCompressor.class, ReiCategories.COMPRESSING,
        BlockEntityCompressor.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuExtractor.class, ReiCategories.EXTRACTING,
        BlockEntityExtractor.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuSawmill.class, ReiCategories.SAWING, BlockEntitySawmill.INPUT_SLOT, 1,
        1));
    registry.register(MachineTransfer.of(MenuAlloySmelter.class, ReiCategories.ALLOY_SMELTING,
        BlockEntityAlloySmelter.INPUT_SLOT_0, 3, 0));
    registry.register(MachineTransfer.of(MenuCoalAlloySmelter.class, ReiCategories.ALLOY_SMELTING,
        BlockEntityAlloySmelter.INPUT_SLOT_0, 3, 0));
    registry.register(MachineTransfer.of(MenuCombustionAlloySmelter.class, ReiCategories.ALLOY_SMELTING,
        BlockEntityAlloySmelter.INPUT_SLOT_0, 3, 0));
    registry.register(MachineTransfer.of(MenuCircuitAssembler.class, ReiCategories.CIRCUIT_ASSEMBLING,
        BlockEntityCircuitAssembler.INPUT_SLOT_0, 3, 0));
    registry.register(MachineTransfer.of(MenuRecycler.class, ReiCategories.RECYCLING, BlockEntityRecycler.INPUT_SLOT,
        1, 1));
    registry.register(MachineTransfer.of(MenuFluidEnricher.class, ReiCategories.FLUID_ENRICHING,
        BlockEntityFluidEnricher.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuOreWashingPlant.class, ReiCategories.ORE_WASHING,
        BlockEntityOreWashingPlant.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuPolymerizer.class, ReiCategories.POLYMERIZING,
        BlockEntityPolymerizer.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuThermalCentrifuge.class, ReiCategories.THERMAL_CENTRIFUGING,
        BlockEntityThermalCentrifuge.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuUraniumCentrifuge.class, ReiCategories.URANIUM_CENTRIFUGING,
        BlockEntityUraniumCentrifuge.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuScanner.class, ReiCategories.SCANNER, BlockEntityScanner.INPUT_SLOT, 1,
        1));
    registry.register(MachineTransfer.of(MenuMetalFormer.class, ReiCategories.ROLLING,
        BlockEntityMetalFormer.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuMetalFormer.class, ReiCategories.CUTTING,
        BlockEntityMetalFormer.INPUT_SLOT, 1, 1));
    registry.register(MachineTransfer.of(MenuMetalFormer.class, ReiCategories.EXTRUDING,
        BlockEntityMetalFormer.INPUT_SLOT, 1, 1));
  }
}
