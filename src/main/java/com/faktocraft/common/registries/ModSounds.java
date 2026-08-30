package com.faktocraft.common.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

  public static final SoundEvent GENERATOR = register("tile.generator");
  public static final SoundEvent GEO_GENERATOR = register("tile.geo_generator");
  public static final SoundEvent WIND_GENERATOR = register("tile.wind_generator");
  public static final SoundEvent OIL_BUBBLE = register("fluid.oil");
  public static final SoundEvent BREAKER_SWITCH = register("block.breaker_switch");
  public static final SoundEvent VALVE_WHEEL = register("block.valve_wheel");
  public static final SoundEvent COMBUSTION_GENERATOR = register("tile.combustion_generator");
  public static final SoundEvent CHARGE_PAD = register("tile.charge_pad");
  public static final SoundEvent CRUSHER = register("tile.crusher");
  public static final SoundEvent COMPRESSOR = register("tile.compressor");
  public static final SoundEvent EXTRACTOR = register("tile.extractor");
  public static final SoundEvent SAWMILL = register("tile.sawmill");
  public static final SoundEvent RECYCLER = register("tile.recycler");
  public static final SoundEvent MATTER_FABRICATOR = register("tile.matter_fabricator");
  public static final SoundEvent ORE_WASHING_PLANT = register("tile.ore_washing_plant");
  public static final SoundEvent METAL_FORMER = register("tile.metal_former");
  public static final SoundEvent REPLICATOR = register("tile.replicator");
  public static final SoundEvent SCANNER = register("tile.scanner");
  public static final SoundEvent THERMAL_CENTRIFUGE = register("tile.thermal_centrifuge");
  public static final SoundEvent DISTILLERY = register("tile.distillery");
  public static final SoundEvent PUMP = register("tile.pump");
  public static final SoundEvent ALLOY_SMELTER = register("tile.alloy_smelter");
  public static final SoundEvent FERMENTER = register("tile.fermenter");
  public static final SoundEvent FLUID_ENRICHER = register("tile.fluid_enricher");
  public static final SoundEvent POLYMERIZER = register("tile.polymerizer");
  public static final SoundEvent CANNING_MACHINE = register("tile.canning_machine");
  public static final SoundEvent EXTRUDER = register("tile.extruder");

  public static final SoundEvent JETPACK = register("item.jetpack");
  public static final SoundEvent TREETAP = register("item.treetap");
  public static final SoundEvent NANO_SABER_IGNITE = register("item.nano_saber_ignite");
  public static final SoundEvent NANO_SABER_HUM = register("item.nano_saber_hum");
  public static final SoundEvent NANO_SABER_RETRACT = register("item.nano_saber_retract");
  public static final SoundEvent WRENCH = register("item.wrench");
  public static final SoundEvent ELECTRIC_WRENCH = register("item.electric_wrench");
  public static final SoundEvent PLUNGER = register("item.plunger");

  public static final SoundEvent NIGHT_VISION = register("player.night_vision");
  public static final SoundEvent MATTER_FABRICATOR_AMPLIFIED = register("extra.matter_fabricator_amplified");

  private static SoundEvent register(String name) {
    ResourceLocation id = RegistrationHandler.id(name);
    return RegistrationHandler.sound(name, SoundEvent.createVariableRangeEvent(id));
  }

  public static void register() {
  }
}
