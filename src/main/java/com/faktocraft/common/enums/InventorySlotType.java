package com.faktocraft.common.enums;

public enum InventorySlotType {
  INPUT("input"),
  NORMAL("normal"),
  OUTPUT("output"),
  BONUS("bonus"),
  ELECTRIC("electric"),
  BATTERY("battery"),
  CAPACITOR("capacitor"),
  TENSION("tension"),
  HELMET("helmet"),
  CHESTPLATE("chestplate"),
  LEGGINGS("leggings"),
  BOOTS("boots"),
  UPGRADE("upgrade"),
  DISABLED("disabled");

  private final String type;

  InventorySlotType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
