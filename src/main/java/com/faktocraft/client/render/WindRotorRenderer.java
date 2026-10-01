package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.generators.wind_generator.BlockEntityWindGenerator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public class WindRotorRenderer implements BlockEntityRenderer<BlockEntityWindGenerator, WindRotorRenderer.State> {

  private static final float MAX_DEGREES_PER_TICK = 45.0F;
  private static final float LINE_WIDTH = 1.0F;
  private static final int RANGE_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.78F, 0.25F);
  private static final int SWEEP_COLOR = ARGB.colorFromFloat(1.0F, 0.35F, 0.85F, 1.0F);

  public static class State extends BlockEntityRenderState {
    boolean overlay;
    @Nullable
    Direction facing;
    boolean hasRotor;
    final ItemStackRenderState rotor = new ItemStackRenderState();
    float yaw;
    float angle;
    int light;
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityWindGenerator generator, State state, float partialTick,
      Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(generator, state, partialTick, cameraPos, breakProgress);
    state.overlay = false;
    state.hasRotor = false;
    state.facing = null;
    if (generator.getLevel() == null) {
      return;
    }
    Direction facing = generator.getBlockState()
        .getValue(com.faktocraft.common.util.BlockStateHelper.horizontalFacingProperty);
    state.facing = facing;
    state.overlay = showsWindMeterOverlay(generator);

    ItemStack rotor = generator.getRotorStack();
    if (rotor.isEmpty()) {
      return;
    }

    float speed = generator.rotorBlocked ? 0.0F
        : (generator.amount > 0 ? 0.30F : 0.06F) * generator.windPercent;

    double now = generator.getLevel().getGameTime() + partialTick;
    if (!Double.isNaN(generator.rotorLastTime)) {
      float elapsed = (float) (now - generator.rotorLastTime);
      if (elapsed > 0) {
        generator.rotorAngle = Mth.wrapDegrees(
            generator.rotorAngle + Math.min(elapsed, 3.0F) * Math.min(speed, MAX_DEGREES_PER_TICK));
      }
    }
    generator.rotorLastTime = now;

    state.yaw = switch (facing) {
      case EAST -> 90.0F;
      case NORTH -> 180.0F;
      case WEST -> -90.0F;
      default -> 0.0F;
    };
    state.angle = generator.rotorAngle;
    state.light = LightCoordsUtil.getLightCoords(generator.getLevel(), generator.getBlockPos().relative(facing));
    RenderStates.item(state.rotor, rotor, generator.getLevel());
    state.hasRotor = true;
  }

  private static boolean showsWindMeterOverlay(BlockEntityWindGenerator generator) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null
        || !(mc.player.getMainHandItem().getItem() instanceof com.faktocraft.common.item.impl.tools.WindMeter
            || mc.player.getOffhandItem().getItem() instanceof com.faktocraft.common.item.impl.tools.WindMeter)) {
      return false;
    }
    Vec3 eye = mc.player.getEyePosition();
    Vec3 reach = eye.add(mc.player.getViewVector(1.0F).scale(mc.player.blockInteractionRange() * 6.0));
    return new AABB(generator.getBlockPos()).clip(eye, reach).isPresent();
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    if (state.overlay && state.facing != null) {
      submitWindMeterOverlay(state.facing, poseStack, collector);
    }
    if (!state.hasRotor) {
      return;
    }
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.rotateDegrees(Axis.YP, state.yaw);
    poseStack.translate(0.0, 0.0, 1.25);
    poseStack.scale(2.0F, 2.0F, 2.0F);
    poseStack.rotateDegrees(Axis.XP, 90.0F);
    poseStack.rotateDegrees(Axis.YP, state.angle);
    state.rotor.submit(poseStack, collector, state.light, OverlayTexture.NO_OVERLAY, 0);
    poseStack.popPose();
  }

  private static void submitWindMeterOverlay(Direction facing, PoseStack poseStack, SubmitNodeCollector collector) {
    int r = BlockEntityWindGenerator.MIN_GENERATOR_DISTANCE;
    collector.submitShapeOutline(poseStack, Shapes.box(-r, 0.0, -r, r + 1.0, 1.0, r + 1.0), RenderTypes.lines(),
        RANGE_COLOR, LINE_WIDTH, false);

    double hubX = 0.5 + facing.getStepX() * 1.25;
    double hubZ = 0.5 + facing.getStepZ() * 1.25;
    int sweep = BlockEntityWindGenerator.ROTOR_SWEEP_RADIUS;
    boolean alongZ = facing.getAxis() == Direction.Axis.Z;
    collector.submitShapeOutline(poseStack, Shapes.box(
        hubX - (alongZ ? sweep : 0.1), 0.5 - sweep, hubZ - (alongZ ? 0.1 : sweep),
        hubX + (alongZ ? sweep : 0.1), 0.5 + sweep, hubZ + (alongZ ? 0.1 : sweep)),
        RenderTypes.lines(), SWEEP_COLOR, LINE_WIDTH, false);
  }

  @Override
  public AABB getRenderBoundingBox(BlockEntityWindGenerator generator) {
    return generator.getRenderBoundingBox();
  }
}
