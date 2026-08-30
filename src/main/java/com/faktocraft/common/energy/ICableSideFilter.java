package com.faktocraft.common.energy;

import net.minecraft.core.Direction;

public interface ICableSideFilter {

  boolean acceptsCableFrom(Direction side);
}
