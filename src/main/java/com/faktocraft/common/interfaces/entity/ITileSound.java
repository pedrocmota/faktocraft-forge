package com.faktocraft.common.interfaces.entity;

import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public interface ITileSound {

  @Nullable
  SoundEvent getSoundEvent();

  default float getVolume() {
    return 0.45F;
  }
}
