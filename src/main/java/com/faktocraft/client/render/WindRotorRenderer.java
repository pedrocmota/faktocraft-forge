package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.generators.wind_generator.BlockEntityWindGenerator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WindRotorRenderer implements BlockEntityRenderer<BlockEntityWindGenerator> {

  private static final float MAX_DEGREES_PER_TICK = 45.0F;

  @Override
  public void render(BlockEntityWindGenerator generator, float partialTick, PoseStack poseStack,
      MultiBufferSource buffer, int packedLight, int packedOverlay) {
    if (generator.getLevel() == null) {
      return;
    }
    renderWindMeterOverlay(generator, poseStack, buffer);

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

    net.minecraft.core.Direction facing = generator.getBlockState()
        .getValue(com.faktocraft.common.util.BlockStateHelper.horizontalFacingProperty);
    float yaw = switch (facing) {
      case EAST -> 90.0F;
      case NORTH -> 180.0F;
      case WEST -> -90.0F;
      default -> 0.0F;
    };

    int light = LevelRenderer.getLightColor(generator.getLevel(), generator.getBlockPos().relative(facing));

    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
    poseStack.translate(0.0, 0.0, 1.25);
    poseStack.scale(2.0F, 2.0F, 2.0F);
    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
    poseStack.mulPose(Axis.YP.rotationDegrees(generator.rotorAngle));
    Minecraft.getInstance().getItemRenderer().renderStatic(rotor, ItemDisplayContext.NONE, light,
        OverlayTexture.NO_OVERLAY, poseStack, buffer, generator.getLevel(), 0);
    poseStack.popPose();
  }

  private void renderWindMeterOverlay(BlockEntityWindGenerator generator, PoseStack poseStack,
      MultiBufferSource buffer) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null
        || !(mc.player.getMainHandItem().getItem() instanceof com.faktocraft.common.item.impl.tools.WindMeter
            || mc.player.getOffhandItem().getItem() instanceof com.faktocraft.common.item.impl.tools.WindMeter)) {
      return;
    }
    net.minecraft.world.phys.Vec3 eye = mc.player.getEyePosition();
    net.minecraft.world.phys.Vec3 reach = eye.add(
        mc.player.getViewVector(1.0F).scale(mc.player.getBlockReach() * 6.0));
    if (new net.minecraft.world.phys.AABB(generator.getBlockPos()).clip(eye, reach).isEmpty()) {
      return;
    }

    com.mojang.blaze3d.vertex.VertexConsumer lines = buffer.getBuffer(
        net.minecraft.client.renderer.RenderType.lines());

    int r = BlockEntityWindGenerator.MIN_GENERATOR_DISTANCE;
    LevelRenderer.renderLineBox(poseStack, lines, -r, 0.0, -r, r + 1.0, 1.0, r + 1.0,
        1.0F, 0.78F, 0.25F, 1.0F);

    net.minecraft.core.Direction facing = generator.getBlockState()
        .getValue(com.faktocraft.common.util.BlockStateHelper.horizontalFacingProperty);
    double hubX = 0.5 + facing.getStepX() * 1.25;
    double hubZ = 0.5 + facing.getStepZ() * 1.25;
    int sweep = BlockEntityWindGenerator.ROTOR_SWEEP_RADIUS;
    boolean alongZ = facing.getAxis() == net.minecraft.core.Direction.Axis.Z;
    LevelRenderer.renderLineBox(poseStack, lines,
        hubX - (alongZ ? sweep : 0.1), 0.5 - sweep, hubZ - (alongZ ? 0.1 : sweep),
        hubX + (alongZ ? sweep : 0.1), 0.5 + sweep, hubZ + (alongZ ? 0.1 : sweep),
        0.35F, 0.85F, 1.0F, 1.0F);
  }
}
