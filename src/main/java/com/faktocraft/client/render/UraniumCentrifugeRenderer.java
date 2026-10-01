package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.BlockEntityUraniumCentrifuge;
import com.faktocraft.common.util.BlockStateHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class UraniumCentrifugeRenderer
    implements BlockEntityRenderer<BlockEntityUraniumCentrifuge, UraniumCentrifugeRenderer.State> {

  public static final Identifier DRUM_MODEL = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "block/uranium_centrifuge_drum");
  public static final Identifier DRUM_ACTIVE_MODEL = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "block/uranium_centrifuge_drum_active");
  public static final Identifier SOCKET_CABLE_MODEL = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "block/uranium_centrifuge_socket_cable");
  public static final StandaloneModelKey<BlockStateModelPart> DRUM_KEY = new StandaloneModelKey<>(
      () -> DRUM_MODEL.toString());
  public static final StandaloneModelKey<BlockStateModelPart> DRUM_ACTIVE_KEY = new StandaloneModelKey<>(
      () -> DRUM_ACTIVE_MODEL.toString());
  public static final StandaloneModelKey<BlockStateModelPart> SOCKET_CABLE_KEY = new StandaloneModelKey<>(
      () -> SOCKET_CABLE_MODEL.toString());

  private static final float TOP_SPEED = 18.0F;
  private static final float SPIN_UP = 0.35F;
  private static final float SPIN_DOWN = 0.2F;

  public static class State extends BlockEntityRenderState {
    boolean valid;
    @Nullable
    BlockStateModelPart drum;
    @Nullable
    BlockStateModelPart socketCable;
    float angle;
    final List<Direction> cables = new ArrayList<>(4);
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityUraniumCentrifuge centrifuge, State state, float partialTick,
      Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(centrifuge, state, partialTick, cameraPos, breakProgress);
    state.valid = false;
    state.cables.clear();
    Level level = centrifuge.getLevel();
    if (level == null) {
      return;
    }
    BlockState blockState = centrifuge.getBlockState();
    boolean active = blockState.hasProperty(BlockStateHelper.activeProperty)
        && blockState.getValue(BlockStateHelper.activeProperty);

    double now = level.getGameTime() + partialTick;
    if (!Double.isNaN(centrifuge.drumLastTime)) {
      float elapsed = Mth.clamp((float) (now - centrifuge.drumLastTime), 0.0F, 3.0F);
      float target = active ? TOP_SPEED : 0.0F;
      float rate = active ? SPIN_UP : SPIN_DOWN;
      centrifuge.drumSpeed = Mth.approach(centrifuge.drumSpeed, target, rate * elapsed);
      centrifuge.drumAngle = Mth.wrapDegrees(centrifuge.drumAngle + centrifuge.drumSpeed * elapsed);
    }
    centrifuge.drumLastTime = now;

    var modelManager = Minecraft.getInstance().getModelManager();
    state.drum = modelManager.getStandaloneModel(active ? DRUM_ACTIVE_KEY : DRUM_KEY);
    state.socketCable = modelManager.getStandaloneModel(SOCKET_CABLE_KEY);
    state.angle = centrifuge.drumAngle;
    for (Direction direction : Direction.Plane.HORIZONTAL) {
      Block neighbor = level.getBlockState(centrifuge.getBlockPos().relative(direction)).getBlock();
      if (neighbor instanceof BlockCable) {
        state.cables.add(direction);
      }
    }
    state.valid = true;
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    if (!state.valid) {
      return;
    }
    int light = state.lightCoords;
    int overlay = OverlayTexture.NO_OVERLAY;

    poseStack.pushPose();
    poseStack.translate(0.5, 0.0, 0.5);
    poseStack.rotateDegrees(Axis.YP, state.angle);
    poseStack.translate(-0.5, 0.0, -0.5);
    RenderStates.submitPart(collector, poseStack, state.drum, light, overlay);
    poseStack.popPose();

    for (Direction direction : state.cables) {
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      switch (direction) {
        case EAST -> poseStack.rotateDegrees(Axis.YP, -90.0F);
        case SOUTH -> poseStack.rotateDegrees(Axis.YP, 180.0F);
        case WEST -> poseStack.rotateDegrees(Axis.YP, 90.0F);
        default -> {
        }
      }
      poseStack.translate(-0.5, -0.5, -0.5);
      RenderStates.submitPart(collector, poseStack, state.socketCable, light, overlay);
      poseStack.popPose();
    }
  }

  @Override
  public AABB getRenderBoundingBox(BlockEntityUraniumCentrifuge centrifuge) {
    return centrifuge.getRenderBoundingBox();
  }
}
