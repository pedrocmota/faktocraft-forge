package com.faktocraft.client.render;

import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.faktocraft.common.util.SpriteUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class FluidPipeRenderer implements BlockEntityRenderer<BlockEntityFluidPipe> {

  private static final float IN0 = 4.8f / 16.0f;
  private static final float IN1 = 11.2f / 16.0f;

  private static final float EPS = 0.002f;

  @Override
  public void render(BlockEntityFluidPipe pipe, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
    ValveWheelRenderer.render(pipe, partialTick, poseStack, buffer);
    PipeSupportRenderer.render(pipe, poseStack, buffer, packedLight, packedOverlay);

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
    final TextureAtlasSprite sprite = SpriteUtil.getFluidSprite(fluid);
    if (sprite == null) {
      return;
    }
    final int color = IClientFluidTypeExtensions.of(fluid).getTintColor() | 0xFF000000;
    final int light = packedLight;
    final float displayFraction = Math.max(0.12f, Math.min(1.0f, fillFraction));
    final float level = ownEmpty ? IN0 : IN0 + (IN1 - IN0) * displayFraction;

    final boolean[] con = new boolean[6];
    for (Direction direction : Direction.values()) {
      con[direction.get3DDataValue()] = pipe.getBlockState()
          .getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction));
    }

    VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
    PoseStack.Pose pose = poseStack.last();

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
          CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN0, IN0, 0.0f, IN1, negLevel, G0);
        }
        if (drawPositive) {
          CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN0, IN0, G1, IN1, posLevel, 1.0f);
        }
      } else if (runAxis == Direction.Axis.X) {
        if (drawNegative) {
          CuboidRenderer.drawBox(pose, vc, sprite, color, light, 0.0f, IN0, IN0, G0, negLevel, IN1);
        }
        if (drawPositive) {
          CuboidRenderer.drawBox(pose, vc, sprite, color, light, G1, IN0, IN0, 1.0f, posLevel, IN1);
        }
      } else {
        float col0 = 6.2f / 16.0f;
        float col1 = 9.8f / 16.0f;
        if (drawNegative) {
          CuboidRenderer.drawBox(pose, vc, sprite, color, light, col0, 0.0f, col0, col1, IN0 - EPS, col1);
          if (negLevel > IN0) {
            CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN0, IN0, IN0, IN1, Math.min(negLevel, G0), IN1);
          }
        }
        if (drawPositive) {
          if (posLevel > G1) {
            CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN0, G1, IN0, IN1, posLevel, IN1);
          }
          if (fillFraction > 0.9f || neighborHasSameFluid(pipe, Direction.UP, fluid)) {
            CuboidRenderer.drawBox(pose, vc, sprite, color, light,
                col0, Math.max(posLevel, G1) + EPS, col0, col1, 1.0f - EPS, col1);
          }
        }
      }
      return;
    }

    CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN0, IN0, IN0, IN1, level, IN1);
    if (con[Direction.NORTH.get3DDataValue()]) {
      CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN0, IN0, 0.0f, IN1, level, IN0);
    }
    if (con[Direction.SOUTH.get3DDataValue()]) {
      CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN0, IN0, IN1, IN1, level, 1.0f);
    }
    if (con[Direction.WEST.get3DDataValue()]) {
      CuboidRenderer.drawBox(pose, vc, sprite, color, light, 0.0f, IN0, IN0, IN0, level, IN1);
    }
    if (con[Direction.EAST.get3DDataValue()]) {
      CuboidRenderer.drawBox(pose, vc, sprite, color, light, IN1, IN0, IN0, 1.0f, level, IN1);
    }
    float c0 = 6.2f / 16.0f;
    float c1 = 9.8f / 16.0f;
    if (con[Direction.DOWN.get3DDataValue()]) {
      CuboidRenderer.drawBox(pose, vc, sprite, color, light, c0, 0.0f, c0, c1, IN0 - EPS, c1);
    }
    if (con[Direction.UP.get3DDataValue()]
        && (fillFraction > 0.9f || neighborHasSameFluid(pipe, Direction.UP, fluid))) {
      CuboidRenderer.drawBox(pose, vc, sprite, color, light, c0, level + EPS, c0, c1, 1.0f - EPS, c1);
    }
  }

  private static boolean neighborHasFluid(BlockEntityFluidPipe pipe, Direction direction) {
    return pipe.getLevel() != null
        && pipe.getLevel().getBlockEntity(pipe.getBlockPos().relative(direction))
            instanceof BlockEntityFluidPipe neighbor
        && !neighbor.tank.isEmpty();
  }

  @org.jetbrains.annotations.Nullable
  private static Fluid runNeighborFluid(BlockEntityFluidPipe pipe) {
    var runAxis = com.faktocraft.common.block.impl.pipe.PipeValveHelper.runAxis(pipe.getBlockState());
    if (runAxis == null || pipe.getLevel() == null) {
      return null;
    }
    for (Direction.AxisDirection axisDirection : Direction.AxisDirection.values()) {
      Direction direction = Direction.fromAxisAndDirection(runAxis, axisDirection);
      if (pipe.getLevel().getBlockEntity(pipe.getBlockPos().relative(direction))
          instanceof BlockEntityFluidPipe neighbor && !neighbor.tank.isEmpty()) {
        return neighbor.tank.getFluid();
      }
    }
    return null;
  }

  private static boolean neighborHasSameFluid(BlockEntityFluidPipe pipe, Direction direction, Fluid fluid) {
    return pipe.getLevel() != null
        && pipe.getLevel().getBlockEntity(pipe.getBlockPos().relative(direction))
            instanceof BlockEntityFluidPipe neighbor
        && !neighbor.tank.isEmpty()
        && neighbor.tank.getFluid().isSame(fluid);
  }

  private static float halfLevel(BlockEntityFluidPipe pipe, Direction direction, float ownLevel, Fluid fluid) {
    if (pipe.getLevel() != null
        && pipe.getLevel().getBlockEntity(pipe.getBlockPos().relative(direction))
            instanceof BlockEntityFluidPipe neighbor
        && !neighbor.tank.isEmpty()
        && neighbor.tank.getFluid().isSame(fluid)) {
      float fraction = (float) neighbor.tank.getFluidAmount() / neighbor.tank.getCapacityMb();
      float display = Math.max(0.12f, Math.min(1.0f, fraction));
      return Math.max(ownLevel, IN0 + (IN1 - IN0) * display);
    }
    return ownLevel;
  }
}
