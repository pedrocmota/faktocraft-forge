package com.faktocraft.client.model;

import com.faktocraft.Faktocraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class HazmatModel extends HumanoidModel<LivingEntity> {

  public static final ModelLayerLocation LAYER = new ModelLayerLocation(
      new ResourceLocation(Faktocraft.MODID, "hazmat"), "main");

  private static HazmatModel instance;

  private final ModelPart respirator;
  private final ModelPart tank;

  public HazmatModel(ModelPart root) {
    super(root);
    this.respirator = root.getChild("head").getChild("respirator");
    this.tank = root.getChild("body").getChild("tank");
  }

  public static HazmatModel get(EquipmentSlot slot) {
    if (instance == null) {
      instance = new HazmatModel(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
    }
    instance.respirator.visible = slot == EquipmentSlot.HEAD;
    instance.tank.visible = slot == EquipmentSlot.CHEST;
    return instance;
  }

  public static LayerDefinition createLayer() {
    MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F);
    PartDefinition head = mesh.getRoot().getChild("head");
    head.addOrReplaceChild("respirator", CubeListBuilder.create()
        .texOffs(0, 44).addBox(-2.0F, -3.0F, -7.0F, 4, 3, 2), PartPose.ZERO);
    PartDefinition body = mesh.getRoot().getChild("body");
    body.addOrReplaceChild("tank", CubeListBuilder.create()
        .texOffs(0, 32).addBox(-3.5F, 1.0F, 3.0F, 7, 8, 3)
        .texOffs(24, 32).addBox(-1.0F, 0.0F, 3.5F, 2, 1, 2), PartPose.ZERO);
    return LayerDefinition.create(mesh, 64, 64);
  }
}
