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
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import java.util.EnumMap;
import java.util.Map;

public class HazmatModel extends HumanoidModel<HumanoidRenderState> {
  public static final ModelLayerLocation LAYER = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "hazmat"), "main");

  private static final Map<EquipmentSlot, HazmatModel> INSTANCES = new EnumMap<>(EquipmentSlot.class);

  private final EquipmentSlot slot;
  private final ModelPart respirator;
  private final ModelPart tank;

  public HazmatModel(ModelPart root, EquipmentSlot slot) {
    super(root);
    this.slot = slot;
    this.respirator = root.getChild("head").getChild("respirator");
    this.tank = root.getChild("body").getChild("tank");
  }

  public static HazmatModel get(EquipmentSlot slot) {
    return INSTANCES.computeIfAbsent(slot,
        s -> new HazmatModel(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER), s));
  }

  @Override
  public void setupAnim(HumanoidRenderState state) {
    super.setupAnim(state);
    ArmorVisibility.apply(this, slot);
    respirator.visible = slot == EquipmentSlot.HEAD;
    tank.visible = slot == EquipmentSlot.CHEST;
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
