package com.faktocraft.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.jetbrains.annotations.Nullable;

public final class StatusMonitorTarget {
  @Nullable
  public static volatile RenderTarget override;
  public static volatile boolean mixinApplied;

  private StatusMonitorTarget() {
  }
}
