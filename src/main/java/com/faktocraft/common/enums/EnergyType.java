package com.faktocraft.common.enums;

public enum EnergyType {
  RECEIVE("receive"),
  EXTRACT("extract"),
  BOTH("both"),
  TRANSFORMER("transformer"),
  CABLE("cable");

  private final String type;

  EnergyType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
