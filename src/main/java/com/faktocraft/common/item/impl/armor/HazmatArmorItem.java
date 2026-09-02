package com.faktocraft.common.item.impl.armor;

import com.faktocraft.common.enums.ModArmorMaterials;
import com.faktocraft.common.item.base.BaseArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class HazmatArmorItem extends BaseArmor implements DyeableLeatherItem {

  public static final int DEFAULT_COLOR = 0xE8C43A;

  public HazmatArmorItem(ArmorItem.Type armorType, Properties properties) {
    super(ModArmorMaterials.HAZMAT, armorType, properties);
  }

  @Override
  public int getColor(ItemStack stack) {
    var tag = stack.getTagElement("display");
    return tag != null && tag.contains("color", 99) ? tag.getInt("color") : DEFAULT_COLOR;
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
        piece.hurtAndBreak(amount, living, entity -> entity.broadcastBreakEvent(slot));
      }
    }
  }
}
