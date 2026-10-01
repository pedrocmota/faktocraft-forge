package com.faktocraft.client.render;

import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.block.impl.pipe.BlockFluidExtractorPipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ExtractorRingRenderer {
  public static final Identifier BAR_MODEL = Identifier.fromNamespaceAndPath(com.faktocraft.Faktocraft.MODID,
      "block/pipe/extractor_ring_bar");
  public static final Identifier CORNERS_MODEL = Identifier.fromNamespaceAndPath(com.faktocraft.Faktocraft.MODID,
      "block/pipe/extractor_ring_corners");
  public static final StandaloneModelKey<BlockStateModelPart> BAR_KEY = new StandaloneModelKey<>(
      () -> BAR_MODEL.toString());
  public static final StandaloneModelKey<BlockStateModelPart> CORNERS_KEY = new StandaloneModelKey<>(
      () -> CORNERS_MODEL.toString());

  public static final class Data {
    boolean present;
    Direction.Axis axis = Direction.Axis.Y;
    final List<Direction> bars = new ArrayList<>(4);
    @Nullable
    BlockStateModelPart corners;
    @Nullable
    BlockStateModelPart bar;
  }

  private ExtractorRingRenderer() {
  }

  public static void extract(BlockEntity pipe, Data data) {
    data.present = false;
    data.bars.clear();
    var level = pipe.getLevel();
    var state = pipe.getBlockState();
    if (level == null || !state.hasProperty(BlockFluidExtractorPipe.SOURCE)) {
      return;
    }
    Direction.Axis axis = ringAxis(state);
    data.axis = axis;
    for (Direction direction : Direction.values()) {
      if (direction.getAxis() == axis || connected(state, direction)) {
        continue;
      }
      data.bars.add(direction);
    }
    var manager = Minecraft.getInstance().getModelManager();
    data.corners = manager.getStandaloneModel(CORNERS_KEY);
    data.bar = manager.getStandaloneModel(BAR_KEY);
    data.present = true;
  }

  public static void submit(@Nullable Data data, PoseStack poseStack, SubmitNodeCollector collector,
      int packedLight, int packedOverlay) {
    if (data == null || !data.present) {
      return;
    }
    Direction.Axis axis = data.axis;
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    rotateToAxis(poseStack, axis);
    poseStack.translate(-0.5, -0.5, -0.5);
    RenderStates.submitPart(collector, poseStack, data.corners, packedLight, packedOverlay);
    poseStack.popPose();

    for (Direction direction : data.bars) {
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      rotateToBar(poseStack, axis, direction);
      poseStack.translate(-0.5, -0.5, -0.5);
      RenderStates.submitPart(collector, poseStack, data.bar, packedLight, packedOverlay);
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
      case Y -> poseStack.rotateDegrees(Axis.XP, 90.0F);
      case X -> poseStack.rotateDegrees(Axis.YP, 90.0F);
      default -> {
      }
    }
  }

  private static void rotateToBar(PoseStack poseStack, Direction.Axis axis, Direction direction) {
    switch (axis) {
      case Z -> {
        switch (direction) {
          case DOWN -> poseStack.rotateDegrees(Axis.ZP, 180.0F);
          case WEST -> poseStack.rotateDegrees(Axis.ZP, 90.0F);
          case EAST -> poseStack.rotateDegrees(Axis.ZP, -90.0F);
          default -> {
          }
        }
      }
      case Y -> {
        switch (direction) {
          case EAST -> poseStack.rotateDegrees(Axis.YP, 90.0F);
          case NORTH -> poseStack.rotateDegrees(Axis.YP, 180.0F);
          case WEST -> poseStack.rotateDegrees(Axis.YP, -90.0F);
          default -> {
          }
        }
        poseStack.rotateDegrees(Axis.XP, 90.0F);
      }
      case X -> {
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        switch (direction) {
          case SOUTH -> poseStack.rotateDegrees(Axis.ZP, 90.0F);
          case DOWN -> poseStack.rotateDegrees(Axis.ZP, 180.0F);
          case NORTH -> poseStack.rotateDegrees(Axis.ZP, -90.0F);
          default -> {
          }
        }
      }
      default -> {
      }
    }
  }
}
