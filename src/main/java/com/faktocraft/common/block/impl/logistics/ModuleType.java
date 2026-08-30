package com.faktocraft.common.block.impl.logistics;

public enum ModuleType {
  SINK("module_sink", 0x4CB20D),
  PROVIDER("module_provider", 0x3E7BBF),
  EXTRACTOR("module_extractor", 0xE68A00),
  SUPPLIER("module_supplier", 0x9C27B0),
  COLLECTOR("module_collector", 0x7E57C2),
  EJECTOR("module_ejector", 0xC87137),
  DISPOSAL("module_disposal", 0x616161);

  private final String id;
  private final int color;

  ModuleType(String id, int color) {
    this.id = id;
    this.color = color;
  }

  public String id() {
    return id;
  }

  public int color() {
    return color;
  }

  public boolean passesAllByDefault() {
    return this == PROVIDER || this == EXTRACTOR || this == COLLECTOR;
  }
}
