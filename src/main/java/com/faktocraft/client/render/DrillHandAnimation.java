package com.faktocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public final class DrillHandAnimation {
  private static final float THRUST_SPEED = 0.9F;
  private static final float THRUST_DEPTH = 0.16F;
  private static final float THRUST_DROP = 0.04F;
  private static final float THRUST_TILT = 7F;
  private static final float SHAKE_SPEED_A = 7.3F;
  private static final float SHAKE_SPEED_B = 5.1F;
  private static final float SHAKE_AMOUNT = 0.012F;
  private static final float ROLL_SPEED = 9F;
  private static final float ROLL_DEGREES = 1.5F;

  private DrillHandAnimation() {
  }

  public static boolean apply(PoseStack poseStack, PlayerRenderState playerState, HumanoidArm arm, ItemStack stack,
      float partialTick, float equipProcess) {
    LocalPlayer player = Minecraft.getInstance().player;
    return player != null && apply(poseStack, player, arm, stack, partialTick, equipProcess);
  }

  public static boolean apply(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack,
      float partialTick, float equipProcess) {
    if (!player.isUsingItem() || player.getUseItem() != stack || player.getUseItemRemainingTicks() <= 0) {
      return false;
    }
    int side = arm == HumanoidArm.RIGHT ? 1 : -1;
    float time = player.tickCount + partialTick;
    float thrust = 0.5F + 0.5F * Mth.sin(time * THRUST_SPEED);
    float shakeX = Mth.sin(time * SHAKE_SPEED_A) * SHAKE_AMOUNT;
    float shakeY = Mth.cos(time * SHAKE_SPEED_B) * SHAKE_AMOUNT;
    poseStack.translate(side * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
    poseStack.translate(shakeX, shakeY - THRUST_DROP * thrust, -THRUST_DEPTH * thrust);
    poseStack.rotateDegrees(Axis.XP, -THRUST_TILT * thrust);
    poseStack.rotateDegrees(Axis.ZP, side * Mth.sin(time * ROLL_SPEED) * ROLL_DEGREES);
    return true;
  }
}
