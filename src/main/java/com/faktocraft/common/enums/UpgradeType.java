package com.faktocraft.common.enums;

public enum UpgradeType {
  OVERCLOCKER("overclocker_upgrade", EnumLang.OVERCLOCKER_UPGRADE),
  EFFICIENCY("efficiency_upgrade", EnumLang.EFFICIENCY_UPGRADE),
  TRANSFORMER("transformer_upgrade", EnumLang.TRANSFORMER_UPGRADE);

  private final String type;
  private final EnumLang lang;

  UpgradeType(String type, EnumLang lang) {
    this.type = type;
    this.lang = lang;
  }

  public String getType() {
    return type;
  }

  public EnumLang getLang() {
    return lang;
  }
}
