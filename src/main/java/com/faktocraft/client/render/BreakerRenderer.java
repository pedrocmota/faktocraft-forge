package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.cable.BlockBreaker;
import com.faktocraft.common.block.impl.cable.BlockEntityBreaker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class BreakerRenderer implements BlockEntityRenderer<BlockEntityBreaker, BreakerRenderer.State> {

  private static final float HANDLE_SPEED = 9.0F;

  public static class State extends BlockEntityRenderState {
    boolean valid;
    final ItemStackRenderState handle = new ItemStackRenderState();
    final ItemStackRenderState dial = new ItemStackRenderState();
    Quaternionf orient = new Quaternionf();
    float yawOnFinal;
    float sweep;
    float yaw;
    int light;
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityBreaker breaker, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(breaker, state, partialTick, cameraPos, breakProgress);
    state.valid = false;
    var level = breaker.getLevel();
    var blockState = breaker.getBlockState();
    if (level == null || !(blockState.getBlock() instanceof BlockBreaker)) {
      return;
    }
    Direction.Axis axis = blockState.getValue(BlockBreaker.AXIS);
    boolean on = blockState.getValue(BlockBreaker.ON);

    float target = on ? 90.0F : 0.0F;
    double now = level.getGameTime() + partialTick;
    if (!Double.isNaN(breaker.handleLastTime)) {
      float elapsed = Math.min(3.0F, (float) (now - breaker.handleLastTime));
      float step = HANDLE_SPEED * Math.max(0.0F, elapsed);
      breaker.handleAngle = breaker.handleAngle < target
          ? Math.min(target, breaker.handleAngle + step)
          : Math.max(target, breaker.handleAngle - step);
    } else {
      breaker.handleAngle = target;
    }
    breaker.handleLastTime = now;

    Direction face = blockState.getValue(BlockBreaker.HANDLE);
    if (face.getAxis() == axis) {
      face = BlockBreaker.handleRing(axis).get(0);
    }

    Quaternionf orient = switch (face) {
      case DOWN -> Axis.XP.rotation((float) Math.toRadians(180.0F));
      case NORTH -> Axis.XP.rotation((float) Math.toRadians(-90.0F));
      case SOUTH -> Axis.XP.rotation((float) Math.toRadians(90.0F));
      case EAST -> Axis.ZP.rotation((float) Math.toRadians(-90.0F));
      case WEST -> Axis.ZP.rotation((float) Math.toRadians(90.0F));
      default -> new Quaternionf();
    };
    org.joml.Vector3f grip = orient.transform(new org.joml.Vector3f(0.0F, 0.0F, -1.0F));
    org.joml.Vector3f axisVec = new org.joml.Vector3f(
        axis == Direction.Axis.X ? 1.0F : 0.0F,
        axis == Direction.Axis.Y ? 1.0F : 0.0F,
        axis == Direction.Axis.Z ? 1.0F : 0.0F);
    float yawOn = Math.abs(grip.dot(axisVec)) > 0.5F ? 0.0F : 90.0F;
    int dialRotation = blockState.getValue(BlockBreaker.DIAL);
    float yawOnFinal = yawOn + (dialRotation & 1) * 180.0F;
    float sweep = (dialRotation & 2) == 0 ? 1.0F : -1.0F;

    state.orient = orient;
    state.yawOnFinal = yawOnFinal;
    state.sweep = sweep;
    state.yaw = yawOnFinal + sweep * (breaker.handleAngle - 90.0F);
    state.light = LightCoordsUtil.getLightCoords(level, breaker.getBlockPos().relative(face));
    RenderStates.item(state.handle, new ItemStack(com.faktocraft.common.registries.PipeRegistry.BREAKER_HANDLE),
        level);
    RenderStates.item(state.dial, new ItemStack(com.faktocraft.common.registries.PipeRegistry.BREAKER_DIAL), level);
    state.valid = true;
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    if (!state.valid) {
      return;
    }
    int light = state.light;
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.rotate(state.orient);
    poseStack.translate(0.0, 1.0, 0.0);

    poseStack.pushPose();
    poseStack.rotateDegrees(Axis.YP, state.yawOnFinal);
    if (state.sweep < 0) {
      poseStack.scale(-1.0F, 1.0F, 1.0F);
    }
    poseStack.translate(0.0, 0.006, 0.0);
    state.dial.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    poseStack.popPose();

    poseStack.rotateDegrees(Axis.YP, state.yaw);
    state.handle.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    poseStack.popPose();
  }
}
