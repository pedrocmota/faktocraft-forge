package com.faktocraft.common.block.impl.logistics;

public enum IoMode {
  PER_UNIT, PER_BATCH, MAINTAIN;

  public static IoMode of(int ordinal) {
    IoMode[] values = values();
    return ordinal >= 0 && ordinal < values.length ? values[ordinal] : PER_UNIT;
  }

  public IoMode next() {
    return of((ordinal() + 1) % values().length);
  }
}
