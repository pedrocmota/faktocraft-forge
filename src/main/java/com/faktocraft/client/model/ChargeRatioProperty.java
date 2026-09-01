package com.faktocraft.client.model;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ChargeRatioProperty {

  public static final ResourceLocation ID = new ResourceLocation(Faktocraft.MODID, "charge_ratio");

  public static void register() {
    ClampedItemPropertyFunction function = (stack, level, entity, seed) -> ElectricItem.getChargeRatio(stack);
    for (Item item : new Item[] {
        ModItems.BATTERY,
        ModItems.ADVANCED_BATTERY,
        ModItems.MEDIUM_BATTERY,
        ModItems.ADVANCED_MEDIUM_BATTERY,
        ModItems.ENERGY_CRYSTAL,
        ModItems.LAPOTRON_CRYSTAL,
        ModItems.ADVANCED_ENERGY_CRYSTAL,
        ModItems.ADVANCED_LAPOTRON_CRYSTAL,
        ModItems.IRIDIUM_CRYSTAL,
        ModItems.CHARGING_BATTERY,
        ModItems.ADVANCED_CHARGING_BATTERY,
        ModItems.CHARGING_ENERGY_CRYSTAL,
        ModItems.CHARGING_LAPOTRON_CRYSTAL }) {
      ItemProperties.register(item, ID, function);
    }
  }
}
