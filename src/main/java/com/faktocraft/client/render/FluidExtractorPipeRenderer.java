package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FluidExtractorPipeRenderer extends FluidPipeRenderer {

  @Override
  public State createRenderState() {
    State state = new State();
    state.socket = new ExtractorSocketRenderer.Data();
    state.ring = new ExtractorRingRenderer.Data();
    return state;
  }

  @Override
  public void extractRenderState(BlockEntityFluidPipe pipe, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    super.extractRenderState(pipe, state, partialTick, cameraPos, breakProgress);
    if (state.socket == null) {
      state.socket = new ExtractorSocketRenderer.Data();
    }
    if (state.ring == null) {
      state.ring = new ExtractorRingRenderer.Data();
    }
    ExtractorSocketRenderer.extract(pipe, state.socket);
    ExtractorRingRenderer.extract(pipe, state.ring);
  }
}
