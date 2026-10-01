package com.faktocraft.common.item.impl.armor;

import com.faktocraft.common.enums.ModArmorMaterials;
import com.faktocraft.common.item.base.BaseArmor;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class HazmatArmorItem extends BaseArmor {
  public static final int DEFAULT_COLOR = 0xE8C43A;

  public HazmatArmorItem(ArmorType armorType, Properties properties) {
    super(ModArmorMaterials.HAZMAT, armorType, properties, HazmatArmorItem::hazmatComponents);
  }

  private static Properties hazmatComponents(Properties properties, ArmorMaterial material, ArmorType armorType) {
    return properties.humanoidArmor(material, armorType).repairable(ModItems.RUBBER);
  }

  public int getColor(ItemStack stack) {
    return DyedItemColor.getOrDefault(stack, DEFAULT_COLOR) & 0xFFFFFF;
  }

  public static boolean hasBoots(LivingEntity living) {
    return living.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof HazmatArmorItem;
  }

  public static boolean isFullSuit(LivingEntity living) {
    return living.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HazmatArmorItem
        && living.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof HazmatArmorItem
        && living.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof HazmatArmorItem
        && living.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof HazmatArmorItem;
  }

  public static void wearFullSuit(LivingEntity living, int amount) {
    for (EquipmentSlot slot : new EquipmentSlot[] {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET }) {
      ItemStack piece = living.getItemBySlot(slot);
      if (piece.getItem() instanceof HazmatArmorItem) {
        piece.hurtAndBreak(amount, living, slot);
      }
    }
  }
}
