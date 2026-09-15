package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import net.minecraft.util.StringRepresentable;

public enum ReactorPart implements StringRepresentable {
  SINGLE(-1, -1, -1),
  X0Y0Z0(0, 0, 0), X1Y0Z0(1, 0, 0), X2Y0Z0(2, 0, 0),
  X0Y0Z1(0, 0, 1), X1Y0Z1(1, 0, 1), X2Y0Z1(2, 0, 1),
  X0Y0Z2(0, 0, 2), X1Y0Z2(1, 0, 2), X2Y0Z2(2, 0, 2),
  X0Y1Z0(0, 1, 0), X1Y1Z0(1, 1, 0), X2Y1Z0(2, 1, 0),
  X0Y1Z1(0, 1, 1), X1Y1Z1(1, 1, 1), X2Y1Z1(2, 1, 1),
  X0Y1Z2(0, 1, 2), X1Y1Z2(1, 1, 2), X2Y1Z2(2, 1, 2),
  X0Y2Z0(0, 2, 0), X1Y2Z0(1, 2, 0), X2Y2Z0(2, 2, 0),
  X0Y2Z1(0, 2, 1), X1Y2Z1(1, 2, 1), X2Y2Z1(2, 2, 1),
  X0Y2Z2(0, 2, 2), X1Y2Z2(1, 2, 2), X2Y2Z2(2, 2, 2);

  public static final int SIZE = 3;

  private final int x;
  private final int y;
  private final int z;

  ReactorPart(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public static ReactorPart of(int x, int y, int z) {
    for (ReactorPart part : values()) {
      if (part.x == x && part.y == y && part.z == z) {
        return part;
      }
    }
    return SINGLE;
  }

  public boolean isSingle() {
    return this == SINGLE;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }

  public int z() {
    return z;
  }

  @Override
  public String getSerializedName() {
    return isSingle() ? "single" : "x" + x + "y" + y + "z" + z;
  }
}
