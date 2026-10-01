package com.faktocraft.client.screens;

import com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenCoalAlloySmelter;
import com.faktocraft.common.block.impl.machines.alloy_smelter.ScreenCombustionAlloySmelter;
import com.faktocraft.common.block.impl.machines.canning_machine.ScreenCanningMachine;
import com.faktocraft.common.block.impl.machines.circuit_assembler.ScreenCircuitAssembler;
import com.faktocraft.common.block.impl.machines.extruder.ScreenExtruder;
import com.faktocraft.common.block.impl.machines.fermenter.ScreenFermenter;
import com.faktocraft.common.block.impl.machines.fluid_enricher.ScreenFluidEnricher;
import com.faktocraft.common.block.impl.machines.metal_former.ScreenMetalFormer;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.NuclearReactorRegistry;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.ScreenNuclearReactor;
import com.faktocraft.common.block.impl.machines.ore_washing_plant.ScreenOreWashingPlant;
import com.faktocraft.common.block.impl.machines.polymerizer.ScreenPolymerizer;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.ScreenThermalCentrifuge;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.ScreenUraniumCentrifuge;
import com.faktocraft.common.registries.machines.M3Registry;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class M3Screens {

  public static void register(RegisterMenuScreensEvent event) {
    event.register(M3Registry.EXTRUDER_MENU, ScreenExtruder::new);
    event.register(M3Registry.FLUID_ENRICHER_MENU, ScreenFluidEnricher::new);
    event.register(
        com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry.DISTILLERY_MENU,
        com.faktocraft.common.block.impl.machines.distillery.ScreenDistillery::new);
    event.register(
        com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.FUELING_STATION_MENU,
        com.faktocraft.common.block.impl.machines.fueling_station.ScreenFuelingStation::new);
    event.register(M3Registry.ALLOY_SMELTER_MENU, ScreenAlloySmelter::new);
    event.register(M3Registry.COAL_ALLOY_SMELTER_MENU, ScreenCoalAlloySmelter::new);
    event.register(M3Registry.COMBUSTION_ALLOY_SMELTER_MENU, ScreenCombustionAlloySmelter::new);
    event.register(M3Registry.CIRCUIT_ASSEMBLER_MENU, ScreenCircuitAssembler::new);
    event.register(M3Registry.FERMENTER_MENU, ScreenFermenter::new);
    event.register(M3Registry.ORE_WASHING_PLANT_MENU, ScreenOreWashingPlant::new);
    event.register(M3Registry.METAL_FORMER_MENU, ScreenMetalFormer::new);
    event.register(M3Registry.THERMAL_CENTRIFUGE_MENU, ScreenThermalCentrifuge::new);
    event.register(M3Registry.URANIUM_CENTRIFUGE_MENU, ScreenUraniumCentrifuge::new);
    event.register(NuclearReactorRegistry.NUCLEAR_REACTOR_MENU, ScreenNuclearReactor::new);
    event.register(M3Registry.CANNING_MACHINE_MENU, ScreenCanningMachine::new);
    event.register(M3Registry.POLYMERIZER_MENU, ScreenPolymerizer::new);
  }
}
