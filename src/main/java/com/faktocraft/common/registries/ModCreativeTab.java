package com.faktocraft.common.registries;

import net.minecraftforge.registries.ForgeRegistries;
import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.block.impl.battery_box.BlockBatteryBox;
import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.block.impl.charge_pad.BlockChargePad;
import com.faktocraft.common.block.impl.transformer.BlockTransformer;
import com.faktocraft.common.config.BasicConfig;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.item.base.EnergyStorageItem;
import com.faktocraft.common.item.base.MaterialItem;
import com.faktocraft.common.item.base.SwordElectricItem;
import com.faktocraft.common.item.base.ToolItem;
import com.faktocraft.common.item.impl.CapacitorItem;
import com.faktocraft.common.item.impl.ChargingBattery;
import com.faktocraft.common.item.impl.Scrap;
import com.faktocraft.common.item.impl.ScrapBox;
import com.faktocraft.common.item.impl.upgrade.ItemUpgrade;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModCreativeTab {

  private static final Set<String> STORAGE_BLOCKS = Set.of(
      "tin_block", "bronze_block", "silver_block", "steel_block", "lead_block", "plastic_block", "rubber_block");
  private static final Set<String> RUBBER_NATURE = Set.of(
      "rubber_log", "rubber_wood", "rubber_leaves", "rubber_sapling");

  private enum Group {
    RESOURCES, MACHINES, ENERGY, TRANSPORT, EQUIPMENT, MISC
  }

  private static final List<String> MACHINE_PROGRESSION = List.of(
      "iron_furnace",
      "electric_furnace", "crusher", "compressor", "extractor", "sawmill", "recycler",
      "metal_former", "extruder", "canning_machine",
      "circuit_assembler", "fluid_enricher", "ore_washing_plant", "distillery", "fueling_station",
      "polymerizer",
      "geological_scanner", "quarry", "landmark", "forester",
      "coal_alloy_smelter", "combustion_alloy_smelter", "alloy_smelter", "fermenter", "thermal_centrifuge",
      "uranium_centrifuge",
      "luminator",
      "matter_fabricator", "scanner", "replicator", "teleport_anchor", "dimensional_teleport_anchor", "chunk_loader",
      "overclocker_upgrade", "advanced_overclocker_upgrade",
      "efficiency_upgrade", "advanced_efficiency_upgrade",
      "tension_upgrade_mk1", "tension_upgrade_mk2", "tension_upgrade_mk3",
      "crude_capacitor", "basic_capacitor", "intermediate_capacitor", "advanced_capacitor");

  private static final List<String> ENERGY_PROGRESSION = List.of(
      "generator", "wind_generator", "geo_generator", "combustion_generator",
      "solar_generator", "advanced_solar_generator", "hybrid_solar_generator", "quantum_solar_generator",
      "stellar_solar_generator", "nuclear_reactor", "empty_fuel_rod", "fuel_rod", "neutron_reflector",
      "depleted_fuel_rod", "nuclear_waste", "plutonium", "nuke",
      "wind_rotor", "advanced_wind_rotor",
      "battery_box", "cesu", "mfe", "mfsu",
      "charge_pad_battery_box", "charge_pad_cesu", "charge_pad_mfe", "charge_pad_mfsu",
      "tin_cable", "tin_cable_insulated", "copper_cable", "copper_cable_insulated",
      "gold_cable", "gold_cable_insulated", "hv_cable", "hv_cable_insulated", "glass_fibre_cable",
      "low_transformer", "medium_transformer", "high_transformer", "very_high_transformer",
      "circuit_breaker",
      "battery", "advanced_battery", "medium_battery", "advanced_medium_battery",
      "energy_crystal", "advanced_energy_crystal",
      "lapotron_crystal", "advanced_lapotron_crystal", "iridium_crystal",
      "charging_battery", "advanced_charging_battery",
      "charging_energy_crystal", "charging_lapotron_crystal");

  private static final List<String> EQUIPMENT_PROGRESSION = List.of(
      "hammer", "cutter", "treetap", "wrench", "toolbox",
      "electric_treetap", "electric_wrench",
      "bronze_sword", "bronze_pickaxe", "bronze_axe", "bronze_shovel", "bronze_hoe",
      "mining_drill", "diamond_drill", "iridium_drill", "hole_drill",
      "chainsaw", "diamond_chainsaw", "iridium_chainsaw",
      "electric_hoe", "multi_tool", "nano_saber",
      "wind_meter", "ie_meter", "prospector", "geiger_counter", "decontaminator", "plunger",
      "memory_card", "teleport_card", "monitor_card",
      "nightvision_goggles",
      "hazmat_helmet", "hazmat_chestplate", "hazmat_leggings", "hazmat_boots",
      "bronze_helmet", "bronze_chestplate", "bronze_leggings", "bronze_boots",
      "nano_helmet", "nano_chestplate", "nano_leggings", "nano_boots",
      "quantum_helmet", "quantum_chestplate", "quantum_leggings", "quantum_boots");

  private static final List<String> TRANSPORT_PROGRESSION = List.of(
      "status_monitor",
      "fluid_extractor_pipe", "fluid_stone_pipe", "fluid_gold_pipe",
      "pipe_valve", "pump", "tank",
      "stone_pipe", "gold_pipe",
      "chassis_pipe_1", "chassis_pipe_2",
      "module_sink", "module_provider", "module_extractor", "module_supplier",
      "module_collector", "module_ejector", "module_disposal", "craft_pipe", "recipe_pipe",
      "logistics_controller", "request_table", "assembly_table",
      "remote_requester", "logistics_throughput_upgrade");

  private static final Set<String> ENERGY_IDS = Set.copyOf(ENERGY_PROGRESSION);
  private static final Set<String> EQUIPMENT_IDS = Set.copyOf(EQUIPMENT_PROGRESSION);
  private static final Set<String> TRANSPORT_IDS = Set.copyOf(TRANSPORT_PROGRESSION);
  private static final Set<String> MACHINE_IDS = Set.copyOf(MACHINE_PROGRESSION);

  public static void register() {
    registerTab("machines", () -> new ItemStack(M2Registry.CRUSHER_ITEM), Group.MACHINES, null);
    registerTab("energy", () -> new ItemStack(M1Registry.MFSU_ITEM), Group.ENERGY, "machines");
    registerTab("transport",
        () -> new ItemStack(com.faktocraft.common.block.impl.logistics.LogisticsRegistry.GOLD_PIPE),
        Group.TRANSPORT, "energy");
    registerTab("equipment", () -> new ItemStack(ModItems.MINING_DRILL), Group.EQUIPMENT, "transport");
    registerTab("resources", () -> new ItemStack(ModItems.IRON_PLATE), Group.RESOURCES, "equipment");
    registerTab("misc", () -> new ItemStack(ModItems.REINFORCED_STONE), Group.MISC, "resources");
  }

  private static void registerTab(String name, Supplier<ItemStack> icon, Group group,
      @org.jetbrains.annotations.Nullable String previousTab) {
    Predicate<Item> filter = item -> classify(item) == group;

    CreativeModeTab.Builder builder = CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.faktocraft." + name))
        .icon(icon);
    if (previousTab != null) {
      builder.withTabsBefore(new net.minecraft.resources.ResourceLocation("faktocraft", previousTab));
    }

    RegistrationHandler.creativeTab(name,
        builder

            .displayItems((parameters, output) -> {
              java.util.List<Item> ordered = new java.util.ArrayList<>();
              for (Item item : ModItems.getAllItems()) {
                if (filter.test(item) && !isUnreleased(item)) {
                  ordered.add(item);
                }
              }
              sortForDisplay(group, ordered);
              for (Item item : ordered) {
                output.accept(item);
                if (item instanceof ElectricItem electricItem && electricItem.getMaxEnergy() > 0) {
                  output.accept(electricItem.makeFullStack());
                } else if (item instanceof com.faktocraft.common.item.base.ElectricArmorItem armorItem
                    && armorItem.getMaxEnergy() > 0) {
                  output.accept(armorItem.makeFullStack());
                }
                if (item == ModItems.FLUID_CELL) {
                  addFilledFluidCells(output);
                }
                if (item instanceof com.faktocraft.common.item.impl.reactor.CoolantCell coolantCell) {
                  output.accept(coolantCell.makeFullStack());
                }
                if (item instanceof com.faktocraft.common.item.impl.armor.JetpackItem jetpack) {
                  ItemStack full = new ItemStack(item);
                  com.faktocraft.common.item.base.FluidItem.setFluid(full,
                      com.faktocraft.common.fluid.ModFluids.FUEL.still(), jetpack.getFluidCapacity());
                  output.accept(full);
                }
              }
            })
            .build());
  }

  private static void addFilledFluidCells(CreativeModeTab.Output output) {
    int capacity = com.faktocraft.common.config.ModConfig.server().fluid_cell_capacity;
    for (net.minecraft.world.level.material.Fluid fluid : net.minecraftforge.registries.ForgeRegistries.FLUIDS) {
      if (fluid == net.minecraft.world.level.material.Fluids.EMPTY
          || !fluid.isSource(fluid.defaultFluidState())) {
        continue;
      }
      ItemStack filled = new ItemStack(ModItems.FLUID_CELL);
      com.faktocraft.common.item.impl.FluidCell.setFluid(filled, fluid, capacity);
      output.accept(filled);
    }
  }

  private static void sortForDisplay(Group group, List<Item> items) {
    switch (group) {
      case MACHINES -> items.sort(Comparator
          .<Item>comparingInt(ModCreativeTab::machineSectionRank)
          .thenComparingInt((Item item) -> progressionRank(MACHINE_PROGRESSION, item))
          .thenComparingInt(ModCreativeTab::machineTierRank));
      case ENERGY -> items.sort(Comparator
          .comparingInt((Item item) -> progressionRank(ENERGY_PROGRESSION, item)));
      case EQUIPMENT -> items.sort(Comparator
          .comparingInt((Item item) -> progressionRank(EQUIPMENT_PROGRESSION, item)));
      case TRANSPORT -> items.sort(Comparator
          .comparingInt((Item item) -> progressionRank(TRANSPORT_PROGRESSION, item)));
      default -> {
      }
    }
  }

  private static int machineSectionRank(Item item) {
    return item instanceof BlockItem ? 0 : 1;
  }

  private static int machineTierRank(Item item) {
    if (item instanceof BlockItem blockItem) {
      Block block = blockItem.getBlock();
      if (block instanceof com.faktocraft.common.interfaces.block.IElectricMachine electric) {
        return electric.getEnergyTier().getLvl();
      }
    }
    return 0;
  }

  private static int progressionRank(List<String> progression, Item item) {
    String path = idOf(item);
    int index = progression.indexOf(path);
    return index < 0 ? progression.size() : index;
  }

  private static String idOf(Item item) {
    ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
    return id == null ? "" : id.getPath();
  }

  private static final Map<String, BooleanSupplier> CONTENT_TOGGLES = Map.of(
      "teleport_anchor", BasicConfig::teleportAnchorEnabled,
      "dimensional_teleport_anchor", BasicConfig::teleportAnchorEnabled,
      "chunk_loader", BasicConfig::chunkLoaderEnabled,
      "nuke", BasicConfig::nukeEnabled);

  public static boolean isUnreleased(Item item) {
    BooleanSupplier toggle = CONTENT_TOGGLES.get(idOf(item));
    if (toggle != null && !toggle.getAsBoolean()) {
      return true;
    }
    return false;
  }

  private static Group classify(Item item) {
    String path = idOf(item);
    if (ENERGY_IDS.contains(path)) {
      return Group.ENERGY;
    }
    if (EQUIPMENT_IDS.contains(path)) {
      return Group.EQUIPMENT;
    }
    if (TRANSPORT_IDS.contains(path)) {
      return Group.TRANSPORT;
    }
    if (MACHINE_IDS.contains(path)) {
      return Group.MACHINES;
    }
    if (item instanceof BlockItem blockItem) {
      Block block = blockItem.getBlock();
      if (block.getClass().getName().startsWith("com.faktocraft.common.block.impl.pipe.")) {
        return Group.TRANSPORT;
      }
      if (block.getClass().getName().startsWith("com.faktocraft.common.block.impl.generators.")
          || block instanceof BlockCable
          || block instanceof BlockTransformer
          || block instanceof BlockBatteryBox
          || block instanceof BlockChargePad) {
        return Group.ENERGY;
      }
      if (block instanceof FaktocraftEntityBlock) {
        return Group.MACHINES;
      }
      if (path.endsWith("_ore") || STORAGE_BLOCKS.contains(path) || RUBBER_NATURE.contains(path)) {
        return Group.RESOURCES;
      }
      return Group.MISC;
    }
    if (item instanceof ArmorItem || item instanceof TieredItem || item instanceof ToolItem
        || item instanceof com.faktocraft.common.item.base.DiggerElectricItem
        || item instanceof SwordElectricItem) {
      return Group.EQUIPMENT;
    }
    if (item instanceof EnergyStorageItem || item instanceof ChargingBattery) {
      return Group.ENERGY;
    }
    if (item instanceof ItemUpgrade || item instanceof CapacitorItem) {
      return Group.MACHINES;
    }
    if (item instanceof MaterialItem || item instanceof Scrap || item instanceof ScrapBox) {
      return Group.RESOURCES;
    }
    return Group.MISC;
  }
}
