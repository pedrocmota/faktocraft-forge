package com.faktocraft.client.render;

import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.block.impl.pipe.BlockFluidExtractorPipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ExtractorRingRenderer {

  public static final ResourceLocation BAR_MODEL = new ResourceLocation(com.faktocraft.Faktocraft.MODID,
      "block/pipe/extractor_ring_bar");
  public static final ResourceLocation CORNERS_MODEL = new ResourceLocation(com.faktocraft.Faktocraft.MODID,
      "block/pipe/extractor_ring_corners");

  private ExtractorRingRenderer() {
  }

  public static void render(BlockEntity pipe, PoseStack poseStack, MultiBufferSource buffer,
      int packedOverlay) {
    var level = pipe.getLevel();
    var state = pipe.getBlockState();
    if (level == null || !state.hasProperty(BlockFluidExtractorPipe.SOURCE)) {
      return;
    }
    var pos = pipe.getBlockPos();
    Direction.Axis axis = ringAxis(state);
    var manager = Minecraft.getInstance().getModelManager();
    var renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
    var consumer = buffer.getBuffer(RenderType.cutout());

    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    rotateToAxis(poseStack, axis);
    poseStack.translate(-0.5, -0.5, -0.5);
    renderer.tesselateBlock(level, manager.getModel(CORNERS_MODEL), state, pos, poseStack,
        consumer, false, level.random, state.getSeed(pos), packedOverlay,
        net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.cutout());
    poseStack.popPose();

    var barModel = manager.getModel(BAR_MODEL);
    for (Direction direction : Direction.values()) {
      if (direction.getAxis() == axis || connected(state, direction)) {
        continue;
      }
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      rotateToBar(poseStack, axis, direction);
      poseStack.translate(-0.5, -0.5, -0.5);
      renderer.tesselateBlock(level, barModel, state, pos, poseStack, consumer, false,
          level.random, state.getSeed(pos), packedOverlay,
          net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.cutout());
      poseStack.popPose();
    }
  }

  private static Direction.Axis ringAxis(BlockState state) {
    Direction source = state.getValue(BlockFluidExtractorPipe.SOURCE);
    if (isRun(state, source, Direction.UP) || isRun(state, source, Direction.DOWN)) {
      return Direction.Axis.Y;
    }
    if (isRun(state, source, Direction.NORTH) || isRun(state, source, Direction.SOUTH)) {
      return Direction.Axis.Z;
    }
    if (isRun(state, source, Direction.EAST) || isRun(state, source, Direction.WEST)) {
      return Direction.Axis.X;
    }
    return source.getAxis();
  }

  private static boolean isRun(BlockState state, Direction source, Direction direction) {
    return direction != source && connected(state, direction);
  }

  private static boolean connected(BlockState state, Direction direction) {
    return state.getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction));
  }

  private static void rotateToAxis(PoseStack poseStack, Direction.Axis axis) {
    switch (axis) {
      case Y -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      case X -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      default -> {
      }
    }
  }

  private static void rotateToBar(PoseStack poseStack, Direction.Axis axis, Direction direction) {
    switch (axis) {
      case Z -> {
        switch (direction) {
          case DOWN -> poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
          case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
          case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
          default -> {
          }
        }
      }
      case Y -> {
        switch (direction) {
          case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
          case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
          case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
          default -> {
          }
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      }
      case X -> {
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        switch (direction) {
          case SOUTH -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
          case DOWN -> poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
          case NORTH -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
          default -> {
          }
        }
      }
      default -> {
      }
    }
  }
}
