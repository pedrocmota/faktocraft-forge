package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.block.impl.machines.uranium_centrifuge.BlockEntityUraniumCentrifuge;
import com.faktocraft.common.util.BlockStateHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class UraniumCentrifugeRenderer implements BlockEntityRenderer<BlockEntityUraniumCentrifuge> {

  public static final ResourceLocation DRUM_MODEL = new ResourceLocation(Faktocraft.MODID,
      "block/uranium_centrifuge_drum");
  public static final ResourceLocation DRUM_ACTIVE_MODEL = new ResourceLocation(Faktocraft.MODID,
      "block/uranium_centrifuge_drum_active");
  public static final ResourceLocation SOCKET_CABLE_MODEL = new ResourceLocation(Faktocraft.MODID,
      "block/uranium_centrifuge_socket_cable");

  private static final float TOP_SPEED = 18.0F;
  private static final float SPIN_UP = 0.35F;
  private static final float SPIN_DOWN = 0.2F;

  @Override
  public void render(BlockEntityUraniumCentrifuge centrifuge, float partialTick, PoseStack poseStack,
      MultiBufferSource buffer, int packedLight, int packedOverlay) {
    Level level = centrifuge.getLevel();
    if (level == null) {
      return;
    }
    BlockState state = centrifuge.getBlockState();
    boolean active = state.hasProperty(BlockStateHelper.activeProperty)
        && state.getValue(BlockStateHelper.activeProperty);

    double now = level.getGameTime() + partialTick;
    if (!Double.isNaN(centrifuge.drumLastTime)) {
      float elapsed = Mth.clamp((float) (now - centrifuge.drumLastTime), 0.0F, 3.0F);
      float target = active ? TOP_SPEED : 0.0F;
      float rate = active ? SPIN_UP : SPIN_DOWN;
      centrifuge.drumSpeed = Mth.approach(centrifuge.drumSpeed, target, rate * elapsed);
      centrifuge.drumAngle = Mth.wrapDegrees(centrifuge.drumAngle + centrifuge.drumSpeed * elapsed);
    }
    centrifuge.drumLastTime = now;

    VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());
    var modelManager = Minecraft.getInstance().getModelManager();

    poseStack.pushPose();
    poseStack.translate(0.5, 0.0, 0.5);
    poseStack.mulPose(Axis.YP.rotationDegrees(centrifuge.drumAngle));
    poseStack.translate(-0.5, 0.0, -0.5);
    tesselate(level, modelManager.getModel(active ? DRUM_ACTIVE_MODEL : DRUM_MODEL), centrifuge, poseStack,
        consumer, packedOverlay);
    poseStack.popPose();

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      Block neighbor = level.getBlockState(centrifuge.getBlockPos().relative(direction)).getBlock();
      if (!(neighbor instanceof BlockCable)) {
        continue;
      }
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      switch (direction) {
        case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        default -> {
        }
      }
      poseStack.translate(-0.5, -0.5, -0.5);
      tesselate(level, modelManager.getModel(SOCKET_CABLE_MODEL), centrifuge, poseStack, consumer, packedOverlay);
      poseStack.popPose();
    }
  }

  private static void tesselate(Level level, BakedModel model, BlockEntityUraniumCentrifuge centrifuge,
      PoseStack poseStack, VertexConsumer consumer, int packedOverlay) {
    BlockState state = centrifuge.getBlockState();
    Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(
        level, model, state, centrifuge.getBlockPos(), poseStack, consumer, false, level.random,
        state.getSeed(centrifuge.getBlockPos()), packedOverlay, ModelData.EMPTY, RenderType.cutout());
  }
}
