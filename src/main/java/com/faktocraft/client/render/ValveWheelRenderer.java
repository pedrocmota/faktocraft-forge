package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.pipe.PipeValve;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.HashMap;
import java.util.Map;

public final class ValveWheelRenderer {

  private static final float WHEEL_SPEED = 20.0F;
  private static final float OPEN_ANGLE = 720.0F;

  private static final Map<BlockPos, float[]> ANGLES = new HashMap<>();

  private ValveWheelRenderer() {
  }

  public static float openProgress(BlockPos pos, boolean fallbackOpen) {
    float[] anim = ANGLES.get(pos);
    return anim != null ? anim[0] / OPEN_ANGLE : (fallbackOpen ? 1.0F : 0.0F);
  }

  public static void render(BlockEntity pipe, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
    var level = pipe.getLevel();
    if (level == null || !(pipe instanceof com.faktocraft.common.block.impl.pipe.IValveHolder holder)) {
      return;
    }
    PipeValve valve = holder.getValve();
    if (!valve.isPresent()) {
      ANGLES.remove(pipe.getBlockPos());
      return;
    }
    if (ANGLES.size() > 1024) {
      ANGLES.clear();
    }

    float target = valve.isOpen() ? OPEN_ANGLE : 0.0F;
    double now = level.getGameTime() + partialTick;
    float[] anim = ANGLES.computeIfAbsent(pipe.getBlockPos().immutable(),
        key -> new float[] { target, (float) now });
    float elapsed = Math.min(3.0F, (float) (now - anim[1]));
    if (elapsed > 0) {
      float step = WHEEL_SPEED * elapsed;
      anim[0] = anim[0] < target ? Math.min(target, anim[0] + step) : Math.max(target, anim[0] - step);
    }
    anim[1] = (float) now;

    Direction face = valve.direction();
    org.joml.Quaternionf orient = switch (face) {
      case DOWN -> Axis.XP.rotationDegrees(180.0F);
      case NORTH -> Axis.XP.rotationDegrees(-90.0F);
      case SOUTH -> Axis.XP.rotationDegrees(90.0F);
      case EAST -> Axis.ZP.rotationDegrees(-90.0F);
      case WEST -> Axis.ZP.rotationDegrees(90.0F);
      default -> new org.joml.Quaternionf();
    };

    ItemStack body = new ItemStack(com.faktocraft.common.registries.PipeRegistry.PIPE_VALVE_BODY);
    ItemStack wheel = new ItemStack(com.faktocraft.common.registries.PipeRegistry.VALVE_WHEEL);
    int light = LevelRenderer.getLightColor(level, pipe.getBlockPos().relative(face));

    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.mulPose(orient);

    poseStack.pushPose();
    Minecraft.getInstance().getItemRenderer().renderStatic(body, ItemDisplayContext.NONE, light,
        OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
    poseStack.popPose();

    var runAxis = com.faktocraft.common.block.impl.pipe.PipeValveHelper.runAxis(pipe.getBlockState());
    if (runAxis != null) {
      org.joml.Vector3f gateNormal = orient.transform(new org.joml.Vector3f(0.0F, 0.0F, 1.0F));
      org.joml.Vector3f axisVec = new org.joml.Vector3f(
          runAxis == Direction.Axis.X ? 1.0F : 0.0F,
          runAxis == Direction.Axis.Y ? 1.0F : 0.0F,
          runAxis == Direction.Axis.Z ? 1.0F : 0.0F);
      float baseYaw = Math.abs(gateNormal.dot(axisVec)) > 0.5F ? 0.0F : 90.0F;
      float progress = anim[0] / OPEN_ANGLE;
      ItemStack gate = new ItemStack(com.faktocraft.common.registries.PipeRegistry.VALVE_GATE);
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(baseYaw + progress * 90.0F));
      Minecraft.getInstance().getItemRenderer().renderStatic(gate, ItemDisplayContext.NONE, light,
          OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
      poseStack.popPose();
    }

    poseStack.translate(0.0, 0.985, 0.0);
    poseStack.mulPose(Axis.YP.rotationDegrees(anim[0]));
    Minecraft.getInstance().getItemRenderer().renderStatic(wheel, ItemDisplayContext.NONE, light,
        OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
    poseStack.popPose();
  }
}
