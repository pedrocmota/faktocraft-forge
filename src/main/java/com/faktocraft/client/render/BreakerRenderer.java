package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.cable.BlockBreaker;
import com.faktocraft.common.block.impl.cable.BlockEntityBreaker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BreakerRenderer implements BlockEntityRenderer<BlockEntityBreaker> {

  private static final float HANDLE_SPEED = 9.0F;

  @Override
  public void render(BlockEntityBreaker breaker, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
    var level = breaker.getLevel();
    var state = breaker.getBlockState();
    if (level == null || !(state.getBlock() instanceof BlockBreaker)) {
      return;
    }
    Direction.Axis axis = state.getValue(BlockBreaker.AXIS);
    boolean on = state.getValue(BlockBreaker.ON);

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

    Direction face = state.getValue(BlockBreaker.HANDLE);
    if (face.getAxis() == axis) {
      face = BlockBreaker.handleRing(axis).get(0);
    }

    org.joml.Quaternionf orient = switch (face) {
      case DOWN -> Axis.XP.rotationDegrees(180.0F);
      case NORTH -> Axis.XP.rotationDegrees(-90.0F);
      case SOUTH -> Axis.XP.rotationDegrees(90.0F);
      case EAST -> Axis.ZP.rotationDegrees(-90.0F);
      case WEST -> Axis.ZP.rotationDegrees(90.0F);
      default -> new org.joml.Quaternionf();
    };
    org.joml.Vector3f grip = orient.transform(new org.joml.Vector3f(0.0F, 0.0F, -1.0F));
    org.joml.Vector3f axisVec = new org.joml.Vector3f(
        axis == Direction.Axis.X ? 1.0F : 0.0F,
        axis == Direction.Axis.Y ? 1.0F : 0.0F,
        axis == Direction.Axis.Z ? 1.0F : 0.0F);
    float yawOn = Math.abs(grip.dot(axisVec)) > 0.5F ? 0.0F : 90.0F;
    int dialRotation = state.getValue(BlockBreaker.DIAL);
    float yawOnFinal = yawOn + (dialRotation & 1) * 180.0F;
    float sweep = (dialRotation & 2) == 0 ? 1.0F : -1.0F;
    float yaw = yawOnFinal + sweep * (breaker.handleAngle - 90.0F);

    ItemStack handle = new ItemStack(com.faktocraft.common.registries.PipeRegistry.BREAKER_HANDLE);
    ItemStack dial = new ItemStack(com.faktocraft.common.registries.PipeRegistry.BREAKER_DIAL);
    int light = LevelRenderer.getLightColor(level, breaker.getBlockPos().relative(face));

    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.mulPose(orient);
    poseStack.translate(0.0, 1.0, 0.0);

    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(yawOnFinal));
    if (sweep < 0) {
      poseStack.scale(-1.0F, 1.0F, 1.0F);
    }
    poseStack.translate(0.0, 0.006, 0.0);
    Minecraft.getInstance().getItemRenderer().renderStatic(dial, ItemDisplayContext.NONE, light,
        OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
    poseStack.popPose();

    poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
    Minecraft.getInstance().getItemRenderer().renderStatic(handle, ItemDisplayContext.NONE, light,
        OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
    poseStack.popPose();
  }

}
