package com.faktocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ExtractorSocketRenderer {

  public static final ResourceLocation SOCKET_MODEL = new ResourceLocation(com.faktocraft.Faktocraft.MODID,
      "block/pipe/extractor_cable_socket");

  private ExtractorSocketRenderer() {
  }

  public static boolean cableAt(net.minecraft.world.level.LevelAccessor level,
      net.minecraft.core.BlockPos pos, Direction direction) {
    return level.getBlockState(pos.relative(direction))
        .getBlock() instanceof com.faktocraft.common.block.impl.cable.BlockCable;
  }

  public static void render(BlockEntity pipe, PoseStack poseStack, MultiBufferSource buffer,
      int packedOverlay) {
    var level = pipe.getLevel();
    if (level == null) {
      return;
    }
    var pos = pipe.getBlockPos();
    var state = pipe.getBlockState();
    var model = Minecraft.getInstance().getModelManager().getModel(SOCKET_MODEL);
    for (Direction direction : Direction.values()) {
      if (!cableAt(level, pos, direction)) {
        continue;
      }
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);

      switch (direction) {
        case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        default -> {
        }
      }
      poseStack.translate(-0.5, -0.5, -0.5);
      Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(
          level, model, state, pos, poseStack, buffer.getBuffer(RenderType.cutout()),
          false, level.random, state.getSeed(pos), packedOverlay,
          net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.cutout());
      poseStack.popPose();
    }
  }
}
