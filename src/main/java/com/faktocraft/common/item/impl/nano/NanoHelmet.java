package com.faktocraft.common.item.impl.nano;

import com.faktocraft.common.interfaces.item.IArmorProperties;
import net.minecraft.world.item.ArmorItem;

public class NanoHelmet extends ItemNanoArmor implements IArmorProperties {

  public NanoHelmet(Properties properties) {
    super(ArmorItem.Type.HELMET, properties);
  }

  @Override
  public boolean supportsNightVision() {
    return true;
  }
}
