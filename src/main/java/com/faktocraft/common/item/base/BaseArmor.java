package com.faktocraft.common.item.base;

import com.faktocraft.common.util.NbtBridge;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

public class BaseArmor extends Item {
  @FunctionalInterface
  protected interface ArmorComponents {
    Properties apply(Properties properties, ArmorMaterial material, ArmorType armorType);
  }

  private final ArmorMaterial material;
  private final ArmorType armorType;

  public BaseArmor(ArmorMaterial material, ArmorType armorType, Properties properties) {
    this(material, armorType, properties, Properties::humanoidArmor);
  }

  protected BaseArmor(ArmorMaterial material, ArmorType armorType, Properties properties, ArmorComponents components) {
    super(components.apply(properties, material, armorType));
    this.material = material;
    this.armorType = armorType;
  }

  protected static Properties equippable(Properties properties, ArmorMaterial material, ArmorType armorType) {
    return properties
        .component(DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot())
            .setEquipSound(material.equipSound())
            .setAsset(material.assetId())
            .build())
        .repairable(material.repairIngredient());
  }

  public ArmorMaterial getMaterial() {
    return material;
  }

  public ArmorType getType() {
    return armorType;
  }

  public EquipmentSlot getEquipmentSlot() {
    return armorType.getSlot();
  }

  @Override
  public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
    NbtBridge.migrateLegacyComponents(stack);
  }
}
