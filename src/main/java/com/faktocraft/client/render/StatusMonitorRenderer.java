package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.BlockStatusMonitor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StatusMonitorRenderer
    implements BlockEntityRenderer<BlockEntityStatusMonitor, StatusMonitorRenderer.State> {

  private static final float SCALE = 1.0F / 64.0F;
  private static final float FRONT_Z = -0.375F + 0.004F;
  private static final float REFRESH_DISTANCE = 48.0F;

  public static class State extends BlockEntityRenderState {
    @Nullable
    Identifier texture;
    Direction facing = Direction.NORTH;
  }

  public StatusMonitorRenderer(BlockEntityRendererProvider.Context context) {
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }

  @Override
  public int getViewDistance() {
    return 96;
  }

  @Override
  public AABB getRenderBoundingBox(BlockEntityStatusMonitor monitor) {
    return new AABB(monitor.getBlockPos()).inflate(BlockStatusMonitor.WIDTH, BlockStatusMonitor.HEIGHT,
        BlockStatusMonitor.WIDTH);
  }

  @Override
  public void extractRenderState(BlockEntityStatusMonitor monitor, State state, float partialTicks,
      Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(monitor, state, partialTicks, cameraPosition, breakProgress);
    state.texture = null;
    BlockState blockState = monitor.getBlockState();
    if (!(blockState.getBlock() instanceof BlockStatusMonitor) || !BlockStatusMonitor.isMaster(blockState)) {
      return;
    }
    boolean near = cameraPosition.distanceToSqr(Vec3.atCenterOf(monitor.getBlockPos()))
        <= REFRESH_DISTANCE * REFRESH_DISTANCE;
    state.texture = StatusMonitorTextures.request(monitor, near);
    state.facing = BlockStatusMonitor.facingOf(blockState);
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    Identifier texture = state.texture;
    if (texture == null) {
      return;
    }
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.rotate(Axis.YP.rotationDegrees(-state.facing.toYRot()));
    poseStack.translate(-0.5F, BlockStatusMonitor.HEIGHT - 0.5F, FRONT_Z);
    poseStack.scale(SCALE, -SCALE, SCALE);
    int w = StatusMonitorContent.PANEL_W;
    int h = StatusMonitorContent.PANEL_H;
    int light = LightCoordsUtil.FULL_BRIGHT;
    collector.submitCustomGeometry(poseStack, RenderTypes.text(texture),
        (PoseStack.Pose pose, VertexConsumer consumer) -> {
          consumer.addVertex(pose, 0, h, 0).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setLight(light);
          consumer.addVertex(pose, w, h, 0).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setLight(light);
          consumer.addVertex(pose, w, 0, 0).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setLight(light);
          consumer.addVertex(pose, 0, 0, 0).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setLight(light);
        });
    poseStack.popPose();
  }
}
