package com.faktocraft.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;

final class ArmorVisibility {
  private ArmorVisibility() {
  }

  static void apply(HumanoidModel<?> model, EquipmentSlot slot) {
    model.head.visible = false;
    model.hat.visible = false;
    model.body.visible = false;
    model.rightArm.visible = false;
    model.leftArm.visible = false;
    model.rightLeg.visible = false;
    model.leftLeg.visible = false;
    switch (slot) {
      case HEAD -> {
        model.head.visible = true;
        model.hat.visible = true;
      }
      case CHEST -> {
        model.body.visible = true;
        model.rightArm.visible = true;
        model.leftArm.visible = true;
      }
      case LEGS -> {
        model.body.visible = true;
        model.rightLeg.visible = true;
        model.leftLeg.visible = true;
      }
      case FEET -> {
        model.rightLeg.visible = true;
        model.leftLeg.visible = true;
      }
      default -> {
      }
    }
  }
}
