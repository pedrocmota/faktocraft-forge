package com.faktocraft.common.cover;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ICoverHost {

  @Nullable
  BlockState getCover();

  int getCoverHoles();

  void setCover(@Nullable BlockState cover, int holes);

  default void setCover(@Nullable BlockState cover) {
    setCover(cover, getCoverHoles());
  }
}
