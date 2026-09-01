package com.faktocraft.common.item.impl.quantum;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.ModArmorMaterials;
import com.faktocraft.common.item.impl.nano.ItemNanoArmor;
import net.minecraft.world.item.ArmorItem;

public class ItemQuantumArmor extends ItemNanoArmor {

  public ItemQuantumArmor(ArmorItem.Type armorType, Properties properties) {
    super(ModArmorMaterials.QUANTUM, armorType, properties, 4000000, EnergyTier.VERY_HIGH);
  }

  @Override
  protected String protectionTooltipKey() {
    return "tooltip." + Faktocraft.MODID + ".quantum_protection";
  }
}
