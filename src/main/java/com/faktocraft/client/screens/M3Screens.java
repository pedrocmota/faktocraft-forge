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
import com.faktocraft.common.block.impl.machines.ore_washing_plant.ScreenOreWashingPlant;
import com.faktocraft.common.block.impl.machines.polymerizer.ScreenPolymerizer;
import com.faktocraft.common.block.impl.machines.thermal_centrifuge.ScreenThermalCentrifuge;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.client.gui.screens.MenuScreens;

public class M3Screens {

  public static void register() {
    MenuScreens.register(M3Registry.EXTRUDER_MENU, ScreenExtruder::new);
    MenuScreens.register(M3Registry.FLUID_ENRICHER_MENU, ScreenFluidEnricher::new);
    MenuScreens.register(
        com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry.DISTILLERY_MENU,
        com.faktocraft.common.block.impl.machines.distillery.ScreenDistillery::new);
    net.minecraft.client.gui.screens.MenuScreens.register(
        com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.FUELING_STATION_MENU,
        com.faktocraft.common.block.impl.machines.fueling_station.ScreenFuelingStation::new);
    MenuScreens.register(M3Registry.ALLOY_SMELTER_MENU, ScreenAlloySmelter::new);
    MenuScreens.register(M3Registry.COAL_ALLOY_SMELTER_MENU, ScreenCoalAlloySmelter::new);
    MenuScreens.register(M3Registry.COMBUSTION_ALLOY_SMELTER_MENU, ScreenCombustionAlloySmelter::new);
    MenuScreens.register(M3Registry.CIRCUIT_ASSEMBLER_MENU, ScreenCircuitAssembler::new);
    MenuScreens.register(M3Registry.FERMENTER_MENU, ScreenFermenter::new);
    MenuScreens.register(M3Registry.ORE_WASHING_PLANT_MENU, ScreenOreWashingPlant::new);
    MenuScreens.register(M3Registry.METAL_FORMER_MENU, ScreenMetalFormer::new);
    MenuScreens.register(M3Registry.THERMAL_CENTRIFUGE_MENU, ScreenThermalCentrifuge::new);
    MenuScreens.register(M3Registry.CANNING_MACHINE_MENU, ScreenCanningMachine::new);
    MenuScreens.register(M3Registry.POLYMERIZER_MENU, ScreenPolymerizer::new);
  }
}
