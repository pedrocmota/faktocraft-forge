package com.faktocraft.client.render;

import com.faktocraft.common.util.SpriteUtil;
import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidPipeRenderer implements BlockEntityRenderer<BlockEntityFluidPipe, FluidPipeRenderer.State> {
  private static final float IN0 = 4.8f / 16.0f;
  private static final float IN1 = 11.2f / 16.0f;

  private static final float EPS = 0.002f;

  public static class State extends BlockEntityRenderState {
    final ValveWheelRenderer.Data valve = new ValveWheelRenderer.Data();
    final PipeSupportRenderer.Data support = new PipeSupportRenderer.Data();
    @Nullable
    ExtractorSocketRenderer.Data socket;
    @Nullable
    ExtractorRingRenderer.Data ring;
    @Nullable
    TextureAtlasSprite sprite;
    int color;
    final List<float[]> boxes = new ArrayList<>();
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityFluidPipe pipe, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(pipe, state, partialTick, cameraPos, breakProgress);
    ValveWheelRenderer.extract(pipe, partialTick, state.valve);
    PipeSupportRenderer.extract(pipe, state.support);
    state.sprite = null;
    state.boxes.clear();

    final boolean ownEmpty = pipe.tank.isEmpty() || pipe.tank.getFluidAmount() <= 0;
    if (ownEmpty && !pipe.getValve().isClosed()) {
      return;
    }
    Fluid fluid = ownEmpty ? runNeighborFluid(pipe) : pipe.tank.getFluid();
    if (fluid == null) {
      return;
    }
    final float fillFraction = ownEmpty ? 0.0f
        : (float) pipe.tank.getFluidAmount() / pipe.tank.getCapacityMb();
    final TextureAtlasSprite sprite = FluidSprites.still(fluid);
    if (sprite == null) {
      return;
    }
    state.color = FluidSprites.tint(fluid) | 0xFF000000;
    final float displayFraction = Math.max(0.12f, Math.min(1.0f, fillFraction));
    final float level = ownEmpty ? IN0 : IN0 + (IN1 - IN0) * displayFraction;

    final boolean[] con = new boolean[6];
    for (Direction direction : Direction.values()) {
      con[direction.get3DDataValue()] = pipe.getBlockState()
          .getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction));
    }
    List<float[]> boxes = state.boxes;

    var valve = pipe.getValve();
    var runAxis = valve.isPresent()
        ? com.faktocraft.common.block.impl.pipe.PipeValveHelper.runAxis(pipe.getBlockState())
        : null;
    float halfGap = runAxis != null
        ? (1.0f - ValveWheelRenderer.openProgress(pipe.getBlockPos(), valve.isOpen())) * (0.55f / 16.0f)
        : 0.0f;
    if (runAxis != null && halfGap > 0.005f) {
      final float G0 = 0.5f - halfGap;
      final float G1 = 0.5f + halfGap;
      Direction fillSide = pipe.getLastFillSide();
      Direction negDir = Direction.fromAxisAndDirection(runAxis, Direction.AxisDirection.NEGATIVE);
      Direction posDir = Direction.fromAxisAndDirection(runAxis, Direction.AxisDirection.POSITIVE);
      boolean drawNegative = ownEmpty ? neighborHasSameFluid(pipe, negDir, fluid)
          : fillSide == null || fillSide == negDir || neighborHasFluid(pipe, negDir);
      boolean drawPositive = ownEmpty ? neighborHasSameFluid(pipe, posDir, fluid)
          : fillSide == null || fillSide == posDir || neighborHasFluid(pipe, posDir);

      float negLevel = halfLevel(pipe, negDir, level, fluid);
      float posLevel = halfLevel(pipe, posDir, level, fluid);
      if (runAxis == Direction.Axis.Z) {
        if (drawNegative) {
          box(boxes, IN0, IN0, 0.0f, IN1, negLevel, G0);
        }
        if (drawPositive) {
          box(boxes, IN0, IN0, G1, IN1, posLevel, 1.0f);
        }
      } else if (runAxis == Direction.Axis.X) {
        if (drawNegative) {
          box(boxes, 0.0f, IN0, IN0, G0, negLevel, IN1);
        }
        if (drawPositive) {
          box(boxes, G1, IN0, IN0, 1.0f, posLevel, IN1);
        }
      } else {
        float col0 = 6.2f / 16.0f;
        float col1 = 9.8f / 16.0f;
        if (drawNegative) {
          box(boxes, col0, 0.0f, col0, col1, IN0 - EPS, col1);
          if (negLevel > IN0) {
            box(boxes, IN0, IN0, IN0, IN1, Math.min(negLevel, G0), IN1);
          }
        }
        if (drawPositive) {
          if (posLevel > G1) {
            box(boxes, IN0, G1, IN0, IN1, posLevel, IN1);
          }
          if (fillFraction > 0.9f || neighborHasSameFluid(pipe, Direction.UP, fluid)) {
            box(boxes, col0, Math.max(posLevel, G1) + EPS, col0, col1, 1.0f - EPS, col1);
          }
        }
      }
      state.sprite = sprite;
      return;
    }

    box(boxes, IN0, IN0, IN0, IN1, level, IN1);
    if (con[Direction.NORTH.get3DDataValue()]) {
      box(boxes, IN0, IN0, 0.0f, IN1, level, IN0);
    }
    if (con[Direction.SOUTH.get3DDataValue()]) {
      box(boxes, IN0, IN0, IN1, IN1, level, 1.0f);
    }
    if (con[Direction.WEST.get3DDataValue()]) {
      box(boxes, 0.0f, IN0, IN0, IN0, level, IN1);
    }
    if (con[Direction.EAST.get3DDataValue()]) {
      box(boxes, IN1, IN0, IN0, 1.0f, level, IN1);
    }
    float c0 = 6.2f / 16.0f;
    float c1 = 9.8f / 16.0f;
    if (con[Direction.DOWN.get3DDataValue()]) {
      box(boxes, c0, 0.0f, c0, c1, IN0 - EPS, c1);
    }
    if (con[Direction.UP.get3DDataValue()]
        && (fillFraction > 0.9f || neighborHasSameFluid(pipe, Direction.UP, fluid))) {
      box(boxes, c0, level + EPS, c0, c1, 1.0f - EPS, c1);
    }
    state.sprite = sprite;
  }

  private static void box(List<float[]> boxes, float x0, float y0, float z0, float x1, float y1, float z1) {
    boxes.add(new float[] { x0, y0, z0, x1, y1, z1 });
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    int light = state.lightCoords;
    int overlay = OverlayTexture.NO_OVERLAY;
    ValveWheelRenderer.submit(state.valve, poseStack, collector);
    PipeSupportRenderer.submit(state.support, poseStack, collector, light, overlay);
    ExtractorSocketRenderer.submit(state.socket, poseStack, collector, light, overlay);
    ExtractorRingRenderer.submit(state.ring, poseStack, collector, light, overlay);

    TextureAtlasSprite sprite = state.sprite;
    if (sprite == null || state.boxes.isEmpty()) {
      return;
    }
    int color = state.color;
    List<float[]> boxes = List.copyOf(state.boxes);
    collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(SpriteUtil.blockAtlas()),
        (pose, vc) -> {
          for (float[] b : boxes) {
            CuboidRenderer.drawBox(pose, vc, sprite, color, light, b[0], b[1], b[2], b[3], b[4], b[5]);
          }
        });
  }

  private static boolean neighborHasFluid(BlockEntityFluidPipe pipe, Direction direction) {
    return pipe.getLevel() != null
        && pipe.getLevel()
            .getBlockEntity(pipe.getBlockPos().relative(direction)) instanceof BlockEntityFluidPipe neighbor
        && !neighbor.tank.isEmpty();
  }

  @Nullable
  private static Fluid runNeighborFluid(BlockEntityFluidPipe pipe) {
    var runAxis = com.faktocraft.common.block.impl.pipe.PipeValveHelper.runAxis(pipe.getBlockState());
    if (runAxis == null || pipe.getLevel() == null) {
      return null;
    }
    for (Direction.AxisDirection axisDirection : Direction.AxisDirection.values()) {
      Direction direction = Direction.fromAxisAndDirection(runAxis, axisDirection);
      if (pipe.getLevel()
          .getBlockEntity(pipe.getBlockPos().relative(direction)) instanceof BlockEntityFluidPipe neighbor
          && !neighbor.tank.isEmpty()) {
        return neighbor.tank.getFluid();
      }
    }
    return null;
  }

  private static boolean neighborHasSameFluid(BlockEntityFluidPipe pipe, Direction direction, Fluid fluid) {
    return pipe.getLevel() != null
        && pipe.getLevel()
            .getBlockEntity(pipe.getBlockPos().relative(direction)) instanceof BlockEntityFluidPipe neighbor
        && !neighbor.tank.isEmpty()
        && neighbor.tank.getFluid().isSame(fluid);
  }

  private static float halfLevel(BlockEntityFluidPipe pipe, Direction direction, float ownLevel, Fluid fluid) {
    if (pipe.getLevel() != null
        && pipe.getLevel()
            .getBlockEntity(pipe.getBlockPos().relative(direction)) instanceof BlockEntityFluidPipe neighbor
        && !neighbor.tank.isEmpty()
        && neighbor.tank.getFluid().isSame(fluid)) {
      float fraction = (float) neighbor.tank.getFluidAmount() / neighbor.tank.getCapacityMb();
      float display = Math.max(0.12f, Math.min(1.0f, fraction));
      return Math.max(ownLevel, IN0 + (IN1 - IN0) * display);
    }
    return ownLevel;
  }
}
