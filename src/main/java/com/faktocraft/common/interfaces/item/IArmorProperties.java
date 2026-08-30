package com.faktocraft.common.interfaces.item;

public interface IArmorProperties {
  default boolean supportsNightVision() {
    return false;
  }
}
