package com.faktocraft.common.util;

public final class EnergyCosts {

  private static final double EPSILON = 1.0e-6;

  private EnergyCosts() {
  }

  public static int perTick(int powerCost, float usageFactor) {
    if (powerCost <= 0) {
      return 0;
    }
    return Math.max(1, (int) Math.ceil(powerCost * (double) usageFactor - EPSILON));
  }
}
