package com.faktocraft.client.render;

import com.faktocraft.common.block.ISupportHost;
import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.registries.PipeRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PipeSupportRenderer {

  private static final float MODEL_POST_TOP = 6.0F;

  private static final float CLEARANCE = 0.05F;

  private PipeSupportRenderer() {
  }

  public static void render(BlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
    if (!(blockEntity instanceof ISupportHost host) || blockEntity.getLevel() == null) {
      return;
    }
    Direction direction = host.supportDirection();
    if (direction == null) {
      return;
    }
    if (!(blockEntity.getBlockState().getBlock() instanceof VoxelBlock voxel)) {
      return;
    }

    float belly = 8.0F - voxel.getApothem() * 16.0F;
    float stretch = Math.max(0.1F, (belly - CLEARANCE) / MODEL_POST_TOP);

    if (voxel instanceof com.faktocraft.common.block.impl.pipe.BlockFluidPipe) {
      renderClamp(blockEntity, poseStack, buffer, packedOverlay);
    }

    var orient = switch (direction) {
      case UP -> Axis.XP.rotationDegrees(180.0F);
      case NORTH -> Axis.XP.rotationDegrees(90.0F);
      case SOUTH -> Axis.XP.rotationDegrees(-90.0F);
      case EAST -> Axis.ZP.rotationDegrees(90.0F);
      case WEST -> Axis.ZP.rotationDegrees(-90.0F);
      default -> new org.joml.Quaternionf();
    };

    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.mulPose(orient);

    poseStack.translate(0.0, -0.5, 0.0);
    poseStack.scale(1.0F, stretch, 1.0F);
    poseStack.translate(0.0, 0.5, 0.0);

    Minecraft.getInstance().getItemRenderer().renderStatic(
        new ItemStack(PipeRegistry.PIPE_SUPPORT), ItemDisplayContext.NONE, packedLight,
        OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
    poseStack.popPose();
  }

  public static final ResourceLocation CLAMP_MODEL = new ResourceLocation(com.faktocraft.Faktocraft.MODID,
      "block/pipe/pipe_clamp");

  private static void renderClamp(BlockEntity blockEntity, PoseStack poseStack,
      MultiBufferSource buffer, int packedOverlay) {
    BlockState state = blockEntity.getBlockState();
    var level = blockEntity.getLevel();
    var pos = blockEntity.getBlockPos();

    java.util.List<Direction> connected = new java.util.ArrayList<>();
    for (Direction direction : Direction.values()) {
      if (connected(state, direction)) {
        connected.add(direction);
      }
    }
    if (connected.size() != 2 || connected.get(0) != connected.get(1).getOpposite()) {
      return;
    }
    var model = Minecraft.getInstance().getModelManager().getModel(CLAMP_MODEL);
    drawClamp(level, state, pos, poseStack, buffer, packedOverlay, model,
        connected.get(0).getAxis(), 0.0F, 0.0F, 0.0F);
  }

  private static void drawClamp(net.minecraft.world.level.Level level, BlockState state,
      net.minecraft.core.BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay,
      net.minecraft.client.resources.model.BakedModel model, Direction.Axis axis,
      float offsetX, float offsetY, float offsetZ) {
    poseStack.pushPose();
    poseStack.translate(0.5 + offsetX, 0.5 + offsetY, 0.5 + offsetZ);
    switch (axis) {
      case X -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      case Y -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
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

  private static boolean connected(BlockState state, Direction direction) {
    var property = VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction);
    return state.hasProperty(property) && state.getValue(property);
  }
}
