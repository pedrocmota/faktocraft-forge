package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.BlockStatusMonitor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class StatusMonitorRenderer implements BlockEntityRenderer<BlockEntityStatusMonitor> {

  private static final float SCALE = 1.0F / 64.0F;
  private static final float FRONT_Z = -0.375F + 0.004F;
  private static final float REFRESH_DISTANCE = 48.0F;

  public StatusMonitorRenderer(BlockEntityRendererProvider.Context context) {
  }

  @Override
  public boolean shouldRenderOffScreen(BlockEntityStatusMonitor monitor) {
    return true;
  }

  @Override
  public int getViewDistance() {
    return 96;
  }

  @Override
  public void render(BlockEntityStatusMonitor monitor, float partialTick, PoseStack poseStack,
      MultiBufferSource buffer, int packedLight, int packedOverlay) {
    BlockState state = monitor.getBlockState();
    if (!(state.getBlock() instanceof BlockStatusMonitor) || !BlockStatusMonitor.isMaster(state)) {
      return;
    }
    boolean near = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera.getPosition()
        .distanceToSqr(monitor.getBlockPos().getCenter()) <= REFRESH_DISTANCE * REFRESH_DISTANCE;
    ResourceLocation texture = StatusMonitorTextures.request(monitor, near);
    if (texture == null) {
      return;
    }
    Direction facing = BlockStatusMonitor.facingOf(state);
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
    poseStack.translate(-0.5F, BlockStatusMonitor.HEIGHT - 0.5F, FRONT_Z);
    poseStack.scale(SCALE, -SCALE, SCALE);
    Matrix4f matrix = poseStack.last().pose();
    VertexConsumer consumer = buffer.getBuffer(RenderType.text(texture));
    int w = StatusMonitorContent.PANEL_W;
    int h = StatusMonitorContent.PANEL_H;
    consumer.vertex(matrix, 0, h, 0).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT)
        .endVertex();
    consumer.vertex(matrix, w, h, 0).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT)
        .endVertex();
    consumer.vertex(matrix, w, 0, 0).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT)
        .endVertex();
    consumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT)
        .endVertex();
    poseStack.popPose();
  }
}
