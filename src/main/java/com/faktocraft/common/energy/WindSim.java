package com.faktocraft.common.energy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;

public final class WindSim {

  private static final int STEP_TICKS = 128;
  private static final double MEAN = 0.30;
  private static final int MAX_CATCHUP_STEPS = 64;

  public static final int SEA_LEVEL = 63;
  public static final int FULL_WIND_HEIGHT = 96;

  private static final Map<ResourceKey<Level>, State> STATES = new HashMap<>();

  private static final class State {
    double strength = MEAN;
    long lastStep = Long.MIN_VALUE;
  }

  private WindSim() {
  }

  public static void clear(ResourceKey<Level> dimension) {
    STATES.remove(dimension);
  }

  public static double getStrength(ServerLevel level) {
    State state = STATES.computeIfAbsent(level.dimension(), key -> new State());
    long now = level.getGameTime();
    if (state.lastStep == Long.MIN_VALUE || now < state.lastStep) {
      state.lastStep = now;
    }

    long steps = (now - state.lastStep) / STEP_TICKS;
    if (steps > MAX_CATCHUP_STEPS) {
      state.lastStep = now - MAX_CATCHUP_STEPS * (long) STEP_TICKS;
      steps = MAX_CATCHUP_STEPS;
    }
    if (steps > 0) {
      RandomSource random = level.getRandom();
      for (long i = 0; i < steps; i++) {
        state.strength += (random.nextDouble() - 0.5) * 0.12 + (MEAN - state.strength) * 0.05;
        state.strength = Mth.clamp(state.strength, 0.0, 1.0);
      }
      state.lastStep += steps * STEP_TICKS;
    }
    return state.strength;
  }

  public static double weatherMultiplier(Level level) {
    if (level.isThundering()) {
      return 1.5;
    }
    if (level.isRaining()) {
      return 1.25;
    }
    return 1.0;
  }

  public static double getEffectiveStrength(ServerLevel level) {
    return Math.min(1.0, getStrength(level) * weatherMultiplier(level));
  }

  public static double heightFactor(int y) {
    return Mth.clamp((y - SEA_LEVEL) / (double) FULL_WIND_HEIGHT, 0.0, 1.0);
  }
}
