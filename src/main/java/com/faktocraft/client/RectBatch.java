package com.faktocraft.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import java.util.Arrays;

public final class RectBatch {
  private static final int STRIDE = 5;

  private final Matrix4f pose;
  private int[] rects = new int[STRIDE * 256];
  private int count;

  public RectBatch(GuiGraphics graphics, int x, int y, int width, int height) {
    this.pose = new Matrix4f(graphics.pose().last().pose());
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

  public void submit(GuiGraphics graphics) {
    if (count == 0) {
      return;
    }
    VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.gui());
    for (int i = 0; i < count; i++) {
      int offset = i * STRIDE;
      int x0 = rects[offset];
      int y0 = rects[offset + 1];
      int x1 = rects[offset + 2];
      int y1 = rects[offset + 3];
      int color = rects[offset + 4];
      consumer.vertex(pose, x0, y0, 0).color(color).endVertex();
      consumer.vertex(pose, x0, y1, 0).color(color).endVertex();
      consumer.vertex(pose, x1, y1, 0).color(color).endVertex();
      consumer.vertex(pose, x1, y0, 0).color(color).endVertex();
    }
    graphics.flush();
  }
}
