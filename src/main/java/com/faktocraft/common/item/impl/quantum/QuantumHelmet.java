package com.faktocraft.common.item.impl.quantum;

import com.faktocraft.common.interfaces.item.IArmorProperties;
import net.minecraft.world.item.ArmorItem;

public class QuantumHelmet extends ItemQuantumArmor implements IArmorProperties {

  public QuantumHelmet(Properties properties) {
    super(ArmorItem.Type.HELMET, properties);
  }

  @Override
  public boolean supportsNightVision() {
    return true;
  }
}
