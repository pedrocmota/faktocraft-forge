package com.faktocraft.common.config;

public class ServerConfig {

  public int low_tier_transfer = 32;
  public int medium_tier_transfer = 128;
  public int high_tier_transfer = 512;
  public int very_high_tier_transfer = 2048;
  public int ultra_tier_transfer = 8192;

  public int low_transformer_step_up_loss_percent = 25;
  public int medium_transformer_step_up_loss_percent = 30;
  public int high_transformer_step_up_loss_percent = 40;
  public int low_transformer_step_down_loss_percent = 5;
  public int medium_transformer_step_down_loss_percent = 8;
  public int high_transformer_step_down_loss_percent = 12;
  public int very_high_transformer_step_down_loss_percent = 15;

  public int wooden_battery_box_capacity = 40_000;
  public int cesu_capacity = 300_000;
  public int mfe_capacity = 4_000_000;
  public int mfsu_capacity = 40_000_000;

  public int generator_energy_capacity = 5000;
  public int generator_tick_generate = 10;

  public int generator_restart_threshold_percent = 85;
  public int priority_generator = 1;
  public int priority_geo_generator = 3;
  public int priority_combustion_generator = 2;
  public int priority_solar_generator = 5;
  public int priority_wind_generator = 5;

  public int geo_generator_energy_capacity = 5000;
  public int geo_generator_lava_capacity = 8000;
  public int geo_generator_tick_generate = 20;

  public int combustion_generator_energy_capacity = 5000;
  public int combustion_generator_fluid_capacity = 8000;
  public int combustion_generator_fuel_tick_generate = 40;
  public int combustion_generator_biogas_tick_generate = 20;

  public int solar_generator_energy_capacity = 5000;
  public int solar_generator_day_tick_generate = 1;
  public int solar_generator_night_tick_generate = 0;
  public int advanced_solar_generator_energy_capacity = 20000;
  public int advanced_solar_generator_day_tick_generate = 6;
  public int advanced_solar_generator_night_tick_generate = 1;
  public int hybrid_solar_generator_energy_capacity = 80000;
  public int hybrid_solar_generator_day_tick_generate = 32;
  public int hybrid_solar_generator_night_tick_generate = 4;
  public int quantum_solar_generator_energy_capacity = 160000;
  public int quantum_solar_generator_day_tick_generate = 192;
  public int quantum_solar_generator_night_tick_generate = 24;
  public int quantum_solar_generator_moonlight_tick_generate = 8;
  public int stellar_solar_generator_energy_capacity = 480000;
  public int stellar_solar_generator_day_tick_generate = 576;
  public int stellar_solar_generator_night_tick_generate = 72;
  public int stellar_solar_generator_moonlight_tick_generate = 24;

  public int circuit_assembler_energy_capacity = 1600;

  public int wind_generator_energy_capacity = 5000;
  public int wind_generator_max_tick_generate = 16;

  public int electric_furnace_energy_capacity = 4000;
  public int electric_furnace_tick_usage = 7;

  public int crusher_energy_capacity = 1200;
  public int extractor_energy_capacity = 1200;
  public int extruder_energy_capacity = 1200;
  public int compressor_energy_capacity = 1200;
  public int sawmill_energy_capacity = 1200;
  public int recycler_energy_capacity = 90;
  public int fluid_enricher_energy_capacity = 1200;

  public int canning_machine_energy_capacity = 1200;
  public int canning_machine_fluid_capacity = 8000;
  public int canning_machine_duration = 30;
  public int canning_machine_tick_usage = 2;

  public int status_monitor_refresh_ticks = 10;

  public int alloy_smelter_energy_capacity = 2400;
  public int alloy_smelter_energy_heat_cost = 50;
  public int combustion_alloy_smelter_fluid_capacity = 8000;
  public int combustion_alloy_smelter_fuel_ticks_per_mb = 2;
  public int combustion_alloy_smelter_biogas_ticks_per_mb = 1;

  public int fermenter_energy_capacity = 2400;
  public int fermenter_biomass_capacity = 10_000;
  public int fermenter_biogas_capacity = 2000;
  public int fermenter_heat_cost = 50;
  public int fermenter_tick_usage = 5;

  public int distillery_energy_capacity = 10_000;

  public int fluid_cell_capacity = 1000;

  public int ore_washing_plant_energy_capacity = 16_000;
  public int ore_washing_plant_fluid_capacity = 8000;

  public int polymerizer_energy_capacity = 16_000;
  public int polymerizer_fluid_capacity = 8000;

  public int matter_fabricator_energy_capacity = 1_000_000;
  public int matter_fabricator_matter_capacity = 10_000;
  public int matter_fabricator_produce_run = 100;

  public int thermal_centrifuge_energy_capacity = 48_000;
  public int thermal_centrifuge_temp_cost = 48;
  public int uranium_centrifuge_energy_capacity = 12_000;

  public boolean radiation_enabled = true;
  public int radiation_interval = 20;
  public int radiation_range = 16;
  public double radiation_safe_dose = 0.5;
  public double radiation_sickness = 30.0;
  public double radiation_chronic_damage = 0.02;
  public double radiation_recovery = 2.0;
  public double radiation_recovery_rate = 0.5;
  public double radiation_acute_dose = 30.0;
  public double radiation_acute_damage = 0.05;
  public double radiation_pocket_factor = 0.5;
  public double radiation_shield_factor = 0.02;
  public double radiation_block_factor = 0.85;
  public double reactor_radiation = 2000.0;
  public double meltdown_radiation = 4000.0;
  public int meltdown_decay_ticks = 72_000;
  public int reactor_energy_capacity = 1_000_000;
  public int reactor_water_capacity = 16_000;
  public int reactor_rod_energy = 40;
  public int reactor_rod_bonus = 30;
  public double reactor_heat_boost = 1.0;
  public int reactor_rod_heat = 6;
  public int reactor_rod_heat_bonus = 3;
  public int reactor_reflector_heat = 3;
  public int reactor_max_heat = 10_000;
  public int reactor_bucket_cooling = 15_000;
  public int reactor_water_per_tick = 4;
  public int reactor_cell_cooling = 6;
  public int reactor_cell_coolant = 3_000;
  public int reactor_cell_life_ticks = 72_000;
  public int reactor_passive_cooling = 2;
  public int reactor_rod_life_ticks = 48_000;
  public double reactor_meltdown_power = 10.0;
  public int priority_nuclear_reactor = 1;
  public int nuke_radius = 45;
  public int nuke_depth = 90;
  public int nuke_fuse_ticks = 120;
  public double nuke_resistance_limit = 1000.0;
  public double nuke_damage = 500.0;
  public double nuke_radiation = 8000.0;
  public int nuke_decay_ticks = 144_000;
  public int nuke_blocks_per_tick = 6000;
  public double ore_radiation = 0.05;

  public int scanner_energy_capacity = 1_000_000;

  public int replicator_energy_capacity = 1_000_000;
  public int replicator_matter_capacity = 100_000;

  public int metal_former_energy_capacity = 4000;

  public int pipe_support_spacing = 10;

  public int logistics_controller_capacity = 40_000;
  public int logistics_energy_per_item = 2;
  public int logistics_energy_per_craft = 20;
  public int logistics_extractor_interval = 20;
  public int logistics_extractor_items_per_op = 16;
  public int logistics_ticks_per_pipe = 4;

  public int logistics_machine_timeout = 2400;
  public int assembly_table_energy_capacity = 4000;
  public int assembly_table_duration = 40;
  public int assembly_table_tick_usage = 8;

  public int luminator_energy_capacity = 40;
  public int luminator_tick_usage = 1;

  public int teleport_anchor_energy_capacity = 200_000;
  public int teleport_anchor_base_cost = 2500;
  public int teleport_anchor_cost_per_block = 10;
  public int teleport_anchor_cooldown_ticks = 40;
  public int teleport_anchor_dimensional_cost = 40_000;
  public int teleport_anchor_preload_radius = 2;
  public int teleport_anchor_preload_ticks = 200;
  public int teleport_anchor_charge_ticks = 100;

  public int hole_drill_ticks = 30;
  public int hole_drill_energy_cost = 200;

  public int chunk_loader_energy_capacity = 100_000;
  public int chunk_loader_tick_usage_per_chunk = 8;
  public int chunk_loader_max_active = 3;

  public int quarry_energy_capacity = 4000;
  public int quarry_energy_per_block = 400;
  public int quarry_max_draw_per_tick = 8;

  public int forester_energy_capacity = 4000;
  public int forester_energy_per_action = 120;
  public int forester_max_draw_per_tick = 8;
}
