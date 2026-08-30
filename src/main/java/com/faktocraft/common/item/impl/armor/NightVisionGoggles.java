package com.faktocraft.common.item.impl.armor;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.ModArmorMaterials;
import com.faktocraft.common.interfaces.item.IArmorProperties;
import com.faktocraft.common.item.base.ElectricArmorItem;
import net.minecraft.world.item.ArmorItem;

public class NightVisionGoggles extends ElectricArmorItem implements IArmorProperties {

  public NightVisionGoggles(Properties properties) {
    super(ModArmorMaterials.NIGHTVISION, ArmorItem.Type.HELMET, properties, 0, 100000,
        EnergyType.RECEIVE, EnergyTier.MEDIUM);
  }

  @Override
  public boolean supportsNightVision() {
    return true;
  }
}
