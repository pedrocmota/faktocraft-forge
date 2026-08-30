package com.faktocraft.common.block;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface ISupportHost {

  int SUPPORT_REFRESH_TICKS = 20;

  @Nullable
  Direction supportDirection();
}
