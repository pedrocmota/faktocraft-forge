package com.faktocraft.common.enums;

public enum TransformerMode {
  STEP_UP(1),
  STEP_DOWN(2);

  private final int id;

  TransformerMode(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static TransformerMode getModeFromId(int id) {
    for (TransformerMode mode : values()) {
      if (mode.id == id) {
        return mode;
      }
    }
    return STEP_UP;
  }
}
