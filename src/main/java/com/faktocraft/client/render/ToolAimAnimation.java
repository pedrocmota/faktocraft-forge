package com.faktocraft.client.render;

import com.faktocraft.client.ToolWorkAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public final class ToolAimAnimation {

  private static final float REST_X = 0.56F;
  private static final float REST_Y = -0.52F;
  private static final float REST_Z = -0.72F;
  private static final float AIM_X = 0.5F;
  private static final float AIM_Y = -0.42F;
  private static final float AIM_Z = -0.72F;
  private static final float EQUIP_DROP = -0.6F;
  private static final float AIM_YAW = 35F;
  private static final float AIM_PITCH = -110F;

  private ToolAimAnimation() {
  }

  public static boolean apply(PoseStack poseStack, HumanoidArm arm, ItemStack stack, float partialTick,
      float equipProcess) {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || arm != player.getMainArm() || !ItemStack.isSameItem(player.getMainHandItem(), stack)) {
      return false;
    }
    float blend = ToolWorkAnimation.blend(player, partialTick);
    if (blend <= 0.0F) {
      return false;
    }
    int side = arm == HumanoidArm.RIGHT ? 1 : -1;
    poseStack.translate(side * Mth.lerp(blend, REST_X, AIM_X),
        Mth.lerp(blend, REST_Y, AIM_Y) + equipProcess * EQUIP_DROP, Mth.lerp(blend, REST_Z, AIM_Z));
    poseStack.rotateDegrees(Axis.YP, side * AIM_YAW * blend);
    poseStack.rotateDegrees(Axis.XP, AIM_PITCH * blend);
    return true;
  }
}
