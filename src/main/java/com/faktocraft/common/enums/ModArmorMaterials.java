package com.faktocraft.common.enums;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.registries.ModTags;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import java.util.EnumMap;
import java.util.Map;

public final class ModArmorMaterials {
  public static final ArmorMaterial BRONZE = new ArmorMaterial(15, defense(2, 5, 6, 2), 9,
      SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ModTags.commonItemTag("ingots/bronze"), asset("bronze"));

  public static final ArmorMaterial HAZMAT = new ArmorMaterial(5, defense(1, 2, 3, 1), 15,
      SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ModTags.itemTag("repairs_nothing"), asset("hazmat"));

  public static final ArmorMaterial JETPACK = new ArmorMaterial(25, defense(0, 0, 3, 0), 9,
      SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ModTags.itemTag("repairs_nothing"), asset("jetpack"));

  public static final ArmorMaterial ADVANCED_JETPACK = new ArmorMaterial(25, defense(0, 0, 3, 0), 9,
      SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ModTags.itemTag("repairs_nothing"), asset("advanced_jetpack"));

  public static final ArmorMaterial NIGHTVISION = new ArmorMaterial(15, defense(3, 0, 0, 0), 9,
      SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, ModTags.itemTag("repairs_nothing"), asset("nightvision"));

  public static final ArmorMaterial NANO = new ArmorMaterial(33, defense(3, 6, 8, 3), 10,
      SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, ModTags.itemTag("repairs_nothing"), asset("nano"));

  public static final ArmorMaterial QUANTUM = new ArmorMaterial(44, defense(4, 7, 9, 4), 12,
      SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, ModTags.itemTag("repairs_nothing"), asset("quantum"));

  private ModArmorMaterials() {
  }

  private static ResourceKey<EquipmentAsset> asset(String name) {
    return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Faktocraft.MODID, name));
  }

  private static Map<ArmorType, Integer> defense(int boots, int legs, int chest, int helmet) {
    Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
    map.put(ArmorType.BOOTS, boots);
    map.put(ArmorType.LEGGINGS, legs);
    map.put(ArmorType.CHESTPLATE, chest);
    map.put(ArmorType.HELMET, helmet);
    return map;
  }
}
