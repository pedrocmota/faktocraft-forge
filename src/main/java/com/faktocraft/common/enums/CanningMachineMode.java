package com.faktocraft.common.enums;

public enum CanningMachineMode {
  FILL(1, "fill", GuiSprite.DOWN_ICON),
  EMPTY(2, "empty", GuiSprite.UP_ICON);

  private final int id;
  private final String type;
  private final GuiSprite sprite;

  CanningMachineMode(int id, String type, GuiSprite sprite) {
    this.id = id;
    this.type = type;
    this.sprite = sprite;
  }

  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public GuiSprite getSprite() {
    return sprite;
  }

  public static CanningMachineMode getModeFromId(int id) {
    for (CanningMachineMode mode : values()) {
      if (mode.id == id) {
        return mode;
      }
    }
    return FILL;
  }
}
