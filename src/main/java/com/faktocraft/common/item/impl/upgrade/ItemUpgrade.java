package com.faktocraft.common.item.impl.upgrade;

import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.interfaces.item.IUpgradeItem;
import com.faktocraft.common.item.base.BaseItem;

public class ItemUpgrade extends BaseItem implements IUpgradeItem {

  private final UpgradeType upgradeType;
  private final boolean advanced;

  public ItemUpgrade(Properties properties, UpgradeType upgradeType) {
    this(properties, upgradeType, false);
  }

  public ItemUpgrade(Properties properties, UpgradeType upgradeType, boolean advanced) {
    super(properties.stacksTo(16));
    this.upgradeType = upgradeType;
    this.advanced = advanced;
  }

  @Override
  public UpgradeType getUpgradeType() {
    return upgradeType;
  }

  @Override
  public boolean isAdvancedUpgrade() {
    return advanced;
  }
}
