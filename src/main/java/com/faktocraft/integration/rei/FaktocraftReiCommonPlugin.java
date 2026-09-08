package com.faktocraft.integration.rei;

import com.faktocraft.common.registries.ModItems;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.forge.REIPluginCommon;

@REIPluginCommon
public class FaktocraftReiCommonPlugin implements REIServerPlugin {

  @Override
  public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
    for (CategoryIdentifier<MachineDisplay> id : ReiCategories.ALL) {
      registry.register(id, BasicDisplay.Serializer.ofSimple(
          (inputs, outputs, location) -> new MachineDisplay(id, inputs, outputs, location, null)));
    }
  }

  @Override
  public void registerItemComparators(ItemComparatorRegistry registry) {
    registry.registerNbt(ModItems.NANO_SABER, ModItems.NANO_HELMET, ModItems.NANO_CHESTPLATE, ModItems.NANO_LEGGINGS,
        ModItems.NANO_BOOTS, ModItems.FLUID_CELL, ModItems.ELECTRIC_HOE, ModItems.ELECTRIC_WRENCH,
        ModItems.ELECTRIC_TREETAP, ModItems.MULTI_TOOL, ModItems.MINING_DRILL, ModItems.DIAMOND_DRILL,
        ModItems.IRIDIUM_DRILL, ModItems.CHAINSAW, ModItems.DIAMOND_CHAINSAW, ModItems.IRIDIUM_CHAINSAW,
        ModItems.BATTERY, ModItems.ADVANCED_BATTERY, ModItems.MEDIUM_BATTERY, ModItems.ADVANCED_MEDIUM_BATTERY,
        ModItems.ENERGY_CRYSTAL, ModItems.LAPOTRON_CRYSTAL, ModItems.ADVANCED_ENERGY_CRYSTAL,
        ModItems.ADVANCED_LAPOTRON_CRYSTAL, ModItems.IRIDIUM_CRYSTAL, ModItems.CHARGING_BATTERY,
        ModItems.ADVANCED_CHARGING_BATTERY, ModItems.CHARGING_ENERGY_CRYSTAL, ModItems.CHARGING_LAPOTRON_CRYSTAL);
  }
}
