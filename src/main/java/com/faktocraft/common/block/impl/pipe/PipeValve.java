package com.faktocraft.common.block.impl.pipe;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum PipeValve implements StringRepresentable {
  NONE("none", null, false),
  DOWN_OPEN("down_open", Direction.DOWN, true),
  DOWN_CLOSED("down_closed", Direction.DOWN, false),
  UP_OPEN("up_open", Direction.UP, true),
  UP_CLOSED("up_closed", Direction.UP, false),
  NORTH_OPEN("north_open", Direction.NORTH, true),
  NORTH_CLOSED("north_closed", Direction.NORTH, false),
  SOUTH_OPEN("south_open", Direction.SOUTH, true),
  SOUTH_CLOSED("south_closed", Direction.SOUTH, false),
  WEST_OPEN("west_open", Direction.WEST, true),
  WEST_CLOSED("west_closed", Direction.WEST, false),
  EAST_OPEN("east_open", Direction.EAST, true),
  EAST_CLOSED("east_closed", Direction.EAST, false);

  private final String name;
  @Nullable
  private final Direction direction;
  private final boolean open;

  PipeValve(String name, @Nullable Direction direction, boolean open) {
    this.name = name;
    this.direction = direction;
    this.open = open;
  }

  public static PipeValve byName(String name) {
    for (PipeValve value : values()) {
      if (value.name.equals(name)) {
        return value;
      }
    }
    return NONE;
  }

  public static PipeValve of(Direction direction, boolean open) {
    for (PipeValve value : values()) {
      if (value.direction == direction && value.open == open) {
        return value;
      }
    }
    return NONE;
  }

  public boolean isPresent() {
    return this != NONE;
  }

  public boolean isOpen() {
    return open;
  }

  public boolean isClosed() {
    return isPresent() && !open;
  }

  @Nullable
  public Direction direction() {
    return direction;
  }

  @Override
  public String getSerializedName() {
    return name;
  }
}
