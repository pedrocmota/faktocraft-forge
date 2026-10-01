package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.pipe.PipeValve;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import java.util.HashMap;
import java.util.Map;

public final class ValveWheelRenderer {
  private static final float WHEEL_SPEED = 20.0F;
  private static final float OPEN_ANGLE = 720.0F;

  private static final Map<BlockPos, float[]> ANGLES = new HashMap<>();

  public static final class Data {
    boolean present;
    final ItemStackRenderState body = new ItemStackRenderState();
    final ItemStackRenderState gate = new ItemStackRenderState();
    final ItemStackRenderState wheel = new ItemStackRenderState();
    Quaternionf orient = new Quaternionf();
    boolean hasGate;
    float gateYaw;
    float wheelAngle;
    int light;
  }

  private ValveWheelRenderer() {
  }

  public static float openProgress(BlockPos pos, boolean fallbackOpen) {
    float[] anim = ANGLES.get(pos);
    return anim != null ? anim[0] / OPEN_ANGLE : (fallbackOpen ? 1.0F : 0.0F);
  }

  public static void extract(BlockEntity pipe, float partialTick, Data data) {
    data.present = false;
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
    Quaternionf orient = switch (face) {
      case DOWN -> Axis.XP.rotation((float) Math.toRadians(180.0F));
      case NORTH -> Axis.XP.rotation((float) Math.toRadians(-90.0F));
      case SOUTH -> Axis.XP.rotation((float) Math.toRadians(90.0F));
      case EAST -> Axis.ZP.rotation((float) Math.toRadians(-90.0F));
      case WEST -> Axis.ZP.rotation((float) Math.toRadians(90.0F));
      default -> new Quaternionf();
    };

    RenderStates.item(data.body, new ItemStack(com.faktocraft.common.registries.PipeRegistry.PIPE_VALVE_BODY), level);
    RenderStates.item(data.wheel, new ItemStack(com.faktocraft.common.registries.PipeRegistry.VALVE_WHEEL), level);
    data.light = LightCoordsUtil.getLightCoords(level, pipe.getBlockPos().relative(face));
    data.orient = orient;
    data.wheelAngle = anim[0];

    var runAxis = com.faktocraft.common.block.impl.pipe.PipeValveHelper.runAxis(pipe.getBlockState());
    data.hasGate = runAxis != null;
    if (runAxis != null) {
      org.joml.Vector3f gateNormal = orient.transform(new org.joml.Vector3f(0.0F, 0.0F, 1.0F));
      org.joml.Vector3f axisVec = new org.joml.Vector3f(
          runAxis == Direction.Axis.X ? 1.0F : 0.0F,
          runAxis == Direction.Axis.Y ? 1.0F : 0.0F,
          runAxis == Direction.Axis.Z ? 1.0F : 0.0F);
      float baseYaw = Math.abs(gateNormal.dot(axisVec)) > 0.5F ? 0.0F : 90.0F;
      float progress = anim[0] / OPEN_ANGLE;
      data.gateYaw = baseYaw + progress * 90.0F;
      RenderStates.item(data.gate, new ItemStack(com.faktocraft.common.registries.PipeRegistry.VALVE_GATE), level);
    }
    data.present = true;
  }

  public static void submit(@Nullable Data data, PoseStack poseStack, SubmitNodeCollector collector) {
    if (data == null || !data.present) {
      return;
    }
    int light = data.light;
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.rotate(data.orient);

    poseStack.pushPose();
    data.body.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    poseStack.popPose();

    if (data.hasGate) {
      poseStack.pushPose();
      poseStack.rotateDegrees(Axis.YP, data.gateYaw);
      data.gate.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
      poseStack.popPose();
    }

    poseStack.translate(0.0, 0.985, 0.0);
    poseStack.rotateDegrees(Axis.YP, data.wheelAngle);
    data.wheel.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    poseStack.popPose();
  }
}
