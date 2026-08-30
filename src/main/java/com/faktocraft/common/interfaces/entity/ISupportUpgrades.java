package com.faktocraft.common.interfaces.entity;

import com.faktocraft.common.enums.UpgradeType;
import java.util.List;

public interface ISupportUpgrades {
  boolean hasUpgrades();

  List<UpgradeType> getSupportedUpgrades();
}
