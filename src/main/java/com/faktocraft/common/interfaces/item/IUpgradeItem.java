package com.faktocraft.common.interfaces.item;

import com.faktocraft.common.enums.UpgradeType;

public interface IUpgradeItem {
  UpgradeType getUpgradeType();

  default boolean isAdvancedUpgrade() {
    return false;
  }
}
