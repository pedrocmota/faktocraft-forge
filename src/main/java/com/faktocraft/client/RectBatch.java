package com.faktocraft.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import java.util.Arrays;

public final class RectBatch implements GuiElementRenderState {
  private static final int STRIDE = 5;

  private final Matrix3x2fc pose;
  @Nullable
  private final ScreenRectangle scissor;
  @Nullable
  private final ScreenRectangle bounds;
  private int[] rects = new int[STRIDE * 256];
  private int count;

  public RectBatch(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
    this.pose = new Matrix3x2f(graphics.pose());
    this.scissor = graphics.peekScissorStack();
    ScreenRectangle area = new ScreenRectangle(x, y, width, height).transformMaxBounds(pose);
    this.bounds = scissor != null ? scissor.intersection(area) : area;
  }

  public void fill(int x0, int y0, int x1, int y1, int color) {
    if (x0 < x1) {
      int tmp = x0;
      x0 = x1;
      x1 = tmp;
    }
    if (y0 < y1) {
      int tmp = y0;
      y0 = y1;
      y1 = tmp;
    }
    int offset = count * STRIDE;
    if (offset + STRIDE > rects.length) {
      rects = Arrays.copyOf(rects, rects.length * 2);
    }
    rects[offset] = x0;
    rects[offset + 1] = y0;
    rects[offset + 2] = x1;
    rects[offset + 3] = y1;
    rects[offset + 4] = color;
    count++;
  }

  public void submit(GuiGraphicsExtractor graphics) {
    if (count > 0 && bounds != null) {
      graphics.submitGuiElementRenderState(this);
    }
  }

  @Override
  public void buildVertices(VertexConsumer consumer) {
    for (int i = 0; i < count; i++) {
      int offset = i * STRIDE;
      int x0 = rects[offset];
      int y0 = rects[offset + 1];
      int x1 = rects[offset + 2];
      int y1 = rects[offset + 3];
      int color = rects[offset + 4];
      consumer.addVertexWith2DPose(pose, x0, y0).setColor(color);
      consumer.addVertexWith2DPose(pose, x0, y1).setColor(color);
      consumer.addVertexWith2DPose(pose, x1, y1).setColor(color);
      consumer.addVertexWith2DPose(pose, x1, y0).setColor(color);
    }
  }

  @Override
  public RenderPipeline pipeline() {
    return RenderPipelines.GUI;
  }

  @Override
  public TextureSetup textureSetup() {
    return TextureSetup.noTexture();
  }

  @Override
  @Nullable
  public ScreenRectangle scissorArea() {
    return scissor;
  }

  @Override
  @Nullable
  public ScreenRectangle bounds() {
    return bounds;
  }
}
