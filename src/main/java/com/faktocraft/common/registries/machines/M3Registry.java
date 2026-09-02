package com.faktocraft.common.registries.machines;

import com.faktocraft.common.block.BlockMachine;
import com.faktocraft.common.block.impl.machines.alloy_smelter.BlockAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.BlockEntityAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.MenuAlloySmelter;
import com.faktocraft.common.block.impl.machines.canning_machine.BlockCanningMachine;
import com.faktocraft.common.block.impl.machines.canning_machine.BlockEntityCanningMachine;
import com.faktocraft.common.block.impl.machines.canning_machine.MenuCanningMachine;
import com.faktocraft.common.block.impl.machines.circuit_assembler.BlockCircuitAssembler;
import com.faktocraft.common.block.impl.machines.circuit_assembler.BlockEntityCircuitAssembler;
import com.faktocraft.common.block.impl.machines.circuit_assembler.MenuCircuitAssembler;
import com.faktocraft.common.block.impl.machines.extruder.BlockEntityExtruder;
import com.faktocraft.common.block.impl.machines.extruder.BlockExtruder;
import com.faktocraft.common.block.impl.machines.extruder.MenuExtruder;
import com.faktocraft.common.block.impl.machines.fermenter.BlockEntityFermenter;
import com.faktocraft.common.block.impl.machines.fermenter.BlockFermenter;
import com.faktocraft.common.block.impl.machines.fermenter.MenuFermenter;
import com.faktocraft.common.block.impl.machines.fluid_enricher.BlockEntityFluidEnricher;
import com.faktocraft.common.block.impl.machines.fluid_enricher.BlockFluidEnricher;
import com.faktocraft.common.block.impl.machines.fluid_enricher.MenuFluidEnricher;
import com.faktocraft.common.block.impl.machines.metal_former.BlockEntityMetalFormer;
import com.faktocraft.common.block.impl.machines.metal_former.BlockMetalFormer;
import com.faktocraft.common.block.impl.machines.metal_former.MenuMetalFormer;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.BlockEntityOreWashingPlant;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.BlockOreWashingPlant;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.MenuOreWashingPlant;
import com.faktocraft.common.block.impl.machines.polymerizer.BlockEntityPolymerizer;
import com.faktocraft.common.block.impl.machines.polymerizer.BlockPolymerizer;
import com.faktocraft.common.block.impl.machines.polymerizer.MenuPolymerizer;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.BlockEntityThermalCentrifuge;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.BlockThermalCentrifuge;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.MenuThermalCentrifuge;
import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class M3Registry {

  public static final Block EXTRUDER = ModBlocks.register("extruder", BlockExtruder::new,
      BlockMachine.machineProperties(12, 0));
  public static final Block FLUID_ENRICHER = ModBlocks.register("fluid_enricher", BlockFluidEnricher::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block ALLOY_SMELTER = ModBlocks.register("alloy_smelter", BlockAlloySmelter::new,
      BlockMachine.machineProperties(12, 0));
  public static final Block CIRCUIT_ASSEMBLER = ModBlocks.register("circuit_assembler", BlockCircuitAssembler::new,
      BlockMachine.machineProperties(8, 0));
  public static final Block FERMENTER = ModBlocks.register("fermenter", BlockFermenter::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block ORE_WASHING_PLANT = ModBlocks.register("ore_washing_plant", BlockOreWashingPlant::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block METAL_FORMER = ModBlocks.register("metal_former", BlockMetalFormer::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block THERMAL_CENTRIFUGE = ModBlocks.register("thermal_centrifuge", BlockThermalCentrifuge::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block CANNING_MACHINE = ModBlocks.register("canning_machine", BlockCanningMachine::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block POLYMERIZER = ModBlocks.register("polymerizer", BlockPolymerizer::new,
      BlockMachine.machineProperties(0, 0));

  public static final Item EXTRUDER_ITEM = ModItems.registerElectricBlockItem(EXTRUDER);
  public static final Item FLUID_ENRICHER_ITEM = ModItems.registerElectricBlockItem(FLUID_ENRICHER);
  public static final Item ALLOY_SMELTER_ITEM = ModItems.registerElectricBlockItem(ALLOY_SMELTER);
  public static final Item CIRCUIT_ASSEMBLER_ITEM = ModItems.registerElectricBlockItem(CIRCUIT_ASSEMBLER);
  public static final Item FERMENTER_ITEM = ModItems.registerElectricBlockItem(FERMENTER);
  public static final Item ORE_WASHING_PLANT_ITEM = ModItems.registerElectricBlockItem(ORE_WASHING_PLANT);
  public static final Item METAL_FORMER_ITEM = ModItems.registerElectricBlockItem(METAL_FORMER);
  public static final Item THERMAL_CENTRIFUGE_ITEM = ModItems.registerElectricBlockItem(THERMAL_CENTRIFUGE);
  public static final Item CANNING_MACHINE_ITEM = ModItems.registerElectricBlockItem(CANNING_MACHINE);
  public static final Item POLYMERIZER_ITEM = ModItems.registerElectricBlockItem(POLYMERIZER);

  public static final BlockEntityType<BlockEntityExtruder> EXTRUDER_BLOCK_ENTITY = registerBlockEntity("extruder",
      BlockEntityExtruder::new, EXTRUDER);
  public static final BlockEntityType<BlockEntityFluidEnricher> FLUID_ENRICHER_BLOCK_ENTITY = registerBlockEntity(
      "fluid_enricher", BlockEntityFluidEnricher::new, FLUID_ENRICHER);
  public static final BlockEntityType<BlockEntityAlloySmelter> ALLOY_SMELTER_BLOCK_ENTITY = registerBlockEntity(
      "alloy_smelter", BlockEntityAlloySmelter::new, ALLOY_SMELTER);
  public static final BlockEntityType<BlockEntityCircuitAssembler> CIRCUIT_ASSEMBLER_BLOCK_ENTITY = registerBlockEntity(
      "circuit_assembler", BlockEntityCircuitAssembler::new, CIRCUIT_ASSEMBLER);
  public static final BlockEntityType<BlockEntityFermenter> FERMENTER_BLOCK_ENTITY = registerBlockEntity("fermenter",
      BlockEntityFermenter::new, FERMENTER);
  public static final BlockEntityType<BlockEntityOreWashingPlant> ORE_WASHING_PLANT_BLOCK_ENTITY = registerBlockEntity(
      "ore_washing_plant", BlockEntityOreWashingPlant::new, ORE_WASHING_PLANT);
  public static final BlockEntityType<BlockEntityMetalFormer> METAL_FORMER_BLOCK_ENTITY = registerBlockEntity(
      "metal_former", BlockEntityMetalFormer::new, METAL_FORMER);
  public static final BlockEntityType<BlockEntityThermalCentrifuge> THERMAL_CENTRIFUGE_BLOCK_ENTITY =
      registerBlockEntity(
          "thermal_centrifuge", BlockEntityThermalCentrifuge::new, THERMAL_CENTRIFUGE);
  public static final BlockEntityType<BlockEntityCanningMachine> CANNING_MACHINE_BLOCK_ENTITY = registerBlockEntity(
      "canning_machine", BlockEntityCanningMachine::new, CANNING_MACHINE);
  public static final BlockEntityType<BlockEntityPolymerizer> POLYMERIZER_BLOCK_ENTITY = registerBlockEntity(
      "polymerizer", BlockEntityPolymerizer::new, POLYMERIZER);

  public static final MenuType<MenuExtruder> EXTRUDER_MENU = MenuTypeHelper.register("extruder",
      (windowId, inv, pos) -> new MenuExtruder(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuFluidEnricher> FLUID_ENRICHER_MENU = MenuTypeHelper.register("fluid_enricher",
      (windowId, inv, pos) -> new MenuFluidEnricher(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuAlloySmelter> ALLOY_SMELTER_MENU = MenuTypeHelper.register("alloy_smelter",
      (windowId, inv, pos) -> new MenuAlloySmelter(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuCircuitAssembler> CIRCUIT_ASSEMBLER_MENU = MenuTypeHelper.register(
      "circuit_assembler",
      (windowId, inv, pos) -> new MenuCircuitAssembler(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuFermenter> FERMENTER_MENU = MenuTypeHelper.register("fermenter",
      (windowId, inv, pos) -> new MenuFermenter(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuOreWashingPlant> ORE_WASHING_PLANT_MENU = MenuTypeHelper.register(
      "ore_washing_plant",
      (windowId, inv, pos) -> new MenuOreWashingPlant(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuMetalFormer> METAL_FORMER_MENU = MenuTypeHelper.register("metal_former",
      (windowId, inv, pos) -> new MenuMetalFormer(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuThermalCentrifuge> THERMAL_CENTRIFUGE_MENU = MenuTypeHelper.register(
      "thermal_centrifuge",
      (windowId, inv, pos) -> new MenuThermalCentrifuge(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuCanningMachine> CANNING_MACHINE_MENU = MenuTypeHelper.register("canning_machine",
      (windowId, inv, pos) -> new MenuCanningMachine(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuPolymerizer> POLYMERIZER_MENU = MenuTypeHelper.register("polymerizer",
      (windowId, inv, pos) -> new MenuPolymerizer(windowId, inv.player.level(), pos, inv, inv.player));

  private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name,
      BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
    return RegistrationHandler.blockEntity(name, factory, blocks);
  }

  public static void register() {
  }
}
