package com.faktocraft.common.item.impl.quantum;

import com.faktocraft.common.interfaces.item.IArmorProperties;
import net.minecraft.world.item.equipment.ArmorType;

public class QuantumHelmet extends ItemQuantumArmor implements IArmorProperties {

  public QuantumHelmet(Properties properties) {
    super(ArmorType.HELMET, properties);
  }

  @Override
  public boolean supportsNightVision() {
    return true;
  }
}
