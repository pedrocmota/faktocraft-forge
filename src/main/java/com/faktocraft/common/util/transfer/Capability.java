package com.faktocraft.common.util.transfer;

public final class Capability<T> {
  private final String name;

  Capability(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Capability[" + name + "]";
  }
}
