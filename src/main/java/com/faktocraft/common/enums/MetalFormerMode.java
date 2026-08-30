package com.faktocraft.common.enums;

public enum MetalFormerMode {
  CUTTING(1, GuiSprite.CUTTING),
  ROLLING(2, GuiSprite.ROLLING),
  EXTRUDING(3, GuiSprite.EXTRUDING);

  private final int id;
  private final GuiSprite sprite;

  MetalFormerMode(int id, GuiSprite sprite) {
    this.id = id;
    this.sprite = sprite;
  }

  public int getId() {
    return id;
  }

  public GuiSprite getSprite() {
    return sprite;
  }

  public static MetalFormerMode getModeFromId(int id) {
    for (MetalFormerMode mode : values()) {
      if (mode.id == id) {
        return mode;
      }
    }
    return CUTTING;
  }
}
