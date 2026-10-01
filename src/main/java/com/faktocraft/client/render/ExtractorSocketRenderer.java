package com.faktocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ExtractorSocketRenderer {
  public static final Identifier SOCKET_MODEL = Identifier.fromNamespaceAndPath(com.faktocraft.Faktocraft.MODID,
      "block/pipe/extractor_cable_socket");
  public static final StandaloneModelKey<BlockStateModelPart> SOCKET_KEY = new StandaloneModelKey<>(
      () -> SOCKET_MODEL.toString());

  public static final class Data {
    final List<Direction> sides = new ArrayList<>(6);
    @Nullable
    BlockStateModelPart socket;
  }

  private ExtractorSocketRenderer() {
  }

  public static boolean cableAt(net.minecraft.world.level.LevelAccessor level,
      net.minecraft.core.BlockPos pos, Direction direction) {
    return level.getBlockState(pos.relative(direction))
        .getBlock() instanceof com.faktocraft.common.block.impl.cable.BlockCable;
  }

  public static void extract(BlockEntity pipe, Data data) {
    data.sides.clear();
    data.socket = null;
    var level = pipe.getLevel();
    if (level == null) {
      return;
    }
    var pos = pipe.getBlockPos();
    for (Direction direction : Direction.values()) {
      if (cableAt(level, pos, direction)) {
        data.sides.add(direction);
      }
    }
    if (!data.sides.isEmpty()) {
      data.socket = Minecraft.getInstance().getModelManager().getStandaloneModel(SOCKET_KEY);
    }
  }

  public static void submit(@Nullable Data data, PoseStack poseStack, SubmitNodeCollector collector,
      int packedLight, int packedOverlay) {
    if (data == null || data.socket == null) {
      return;
    }
    for (Direction direction : data.sides) {
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);

      switch (direction) {
        case EAST -> poseStack.rotateDegrees(Axis.YP, -90.0F);
        case SOUTH -> poseStack.rotateDegrees(Axis.YP, 180.0F);
        case WEST -> poseStack.rotateDegrees(Axis.YP, 90.0F);
        case UP -> poseStack.rotateDegrees(Axis.XP, 90.0F);
        case DOWN -> poseStack.rotateDegrees(Axis.XP, -90.0F);
        default -> {
        }
      }
      poseStack.translate(-0.5, -0.5, -0.5);
      RenderStates.submitPart(collector, poseStack, data.socket, packedLight, packedOverlay);
      poseStack.popPose();
    }
  }
}
