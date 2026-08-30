package com.faktocraft.common.enums;

import com.faktocraft.IndReb;
import com.faktocraft.common.registries.ModTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.util.Lazy;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {

  BRONZE("bronze", 15, defense(2, 5, 6, 2), 9,
      SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F,
      () -> Ingredient.of(ModTags.commonItemTag("ingots/bronze"))),

  HAZMAT("hazmat", 5, defense(1, 2, 3, 1), 15,
      SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
      () -> Ingredient.of(com.faktocraft.common.registries.ModItems.RUBBER)),

  JETPACK("jetpack", 25, defense(0, 0, 3, 0), 9,
      SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F,
      () -> Ingredient.of(ModTags.itemTag("repairs_nothing"))),

  NIGHTVISION("nightvision", 15, defense(3, 0, 0, 0), 9,
      SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F,
      () -> Ingredient.of(ModTags.itemTag("repairs_nothing"))),

  NANO("nano", 33, defense(3, 6, 8, 3), 10,
      SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F,
      () -> Ingredient.of(ModTags.itemTag("repairs_nothing"))),

  QUANTUM("quantum", 44, defense(4, 7, 9, 4), 12,
      SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F,
      () -> Ingredient.of(ModTags.itemTag("repairs_nothing")));

  private static final Map<ArmorItem.Type, Integer> HEALTH_FOR_TYPE = Map.of(
      ArmorItem.Type.BOOTS, 13,
      ArmorItem.Type.LEGGINGS, 15,
      ArmorItem.Type.CHESTPLATE, 16,
      ArmorItem.Type.HELMET, 11);

  private final String name;
  private final int durabilityMultiplier;
  private final Map<ArmorItem.Type, Integer> protectionForType;
  private final int enchantmentValue;
  private final SoundEvent equipSound;
  private final float toughness;
  private final float knockbackResistance;
  private final Lazy<Ingredient> repairIngredient;

  ModArmorMaterials(String name, int durabilityMultiplier, Map<ArmorItem.Type, Integer> protectionForType,
      int enchantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance,
      Supplier<Ingredient> repairIngredient) {
    this.name = name;
    this.durabilityMultiplier = durabilityMultiplier;
    this.protectionForType = protectionForType;
    this.enchantmentValue = enchantmentValue;
    this.equipSound = equipSound;
    this.toughness = toughness;
    this.knockbackResistance = knockbackResistance;
    this.repairIngredient = Lazy.of(repairIngredient);
  }

  private static Map<ArmorItem.Type, Integer> defense(int boots, int legs, int chest, int helmet) {
    Map<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
    map.put(ArmorItem.Type.BOOTS, boots);
    map.put(ArmorItem.Type.LEGGINGS, legs);
    map.put(ArmorItem.Type.CHESTPLATE, chest);
    map.put(ArmorItem.Type.HELMET, helmet);
    return map;
  }

  @Override
  public int getDurabilityForType(ArmorItem.Type type) {
    return HEALTH_FOR_TYPE.get(type) * durabilityMultiplier;
  }

  @Override
  public int getDefenseForType(ArmorItem.Type type) {
    return protectionForType.get(type);
  }

  @Override
  public int getEnchantmentValue() {
    return enchantmentValue;
  }

  @Override
  public SoundEvent getEquipSound() {
    return equipSound;
  }

  @Override
  public Ingredient getRepairIngredient() {
    return repairIngredient.get();
  }

  @Override
  public String getName() {
    return IndReb.MODID + ":" + name;
  }

  @Override
  public float getToughness() {
    return toughness;
  }

  @Override
  public float getKnockbackResistance() {
    return knockbackResistance;
  }
}
