package com.faktocraft.client.render;

import com.faktocraft.common.block.ISupportHost;
import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.registries.PipeRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public final class PipeSupportRenderer {
  private static final float MODEL_POST_TOP = 6.0F;

  private static final float CLEARANCE = 0.05F;

  public static final Identifier CLAMP_MODEL = Identifier.fromNamespaceAndPath(com.faktocraft.Faktocraft.MODID,
      "block/pipe/pipe_clamp");
  public static final StandaloneModelKey<BlockStateModelPart> CLAMP_KEY = new StandaloneModelKey<>(
      () -> CLAMP_MODEL.toString());

  public static final class Data {
    boolean present;
    final ItemStackRenderState support = new ItemStackRenderState();
    Quaternionf orient = new Quaternionf();
    float stretch;
    @Nullable
    Direction.Axis clampAxis;
    @Nullable
    BlockStateModelPart clamp;
  }

  private PipeSupportRenderer() {
  }

  public static void extract(BlockEntity blockEntity, Data data) {
    data.present = false;
    data.clampAxis = null;
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
    data.stretch = Math.max(0.1F, (belly - CLEARANCE) / MODEL_POST_TOP);

    if (voxel instanceof com.faktocraft.common.block.impl.pipe.BlockFluidPipe) {
      extractClamp(blockEntity, data);
    }

    data.orient = switch (direction) {
      case UP -> Axis.XP.rotation((float) Math.toRadians(180.0F));
      case NORTH -> Axis.XP.rotation((float) Math.toRadians(90.0F));
      case SOUTH -> Axis.XP.rotation((float) Math.toRadians(-90.0F));
      case EAST -> Axis.ZP.rotation((float) Math.toRadians(90.0F));
      case WEST -> Axis.ZP.rotation((float) Math.toRadians(-90.0F));
      default -> new Quaternionf();
    };
    RenderStates.item(data.support, new ItemStack(PipeRegistry.PIPE_SUPPORT), blockEntity.getLevel());
    data.present = true;
  }

  public static void submit(@Nullable Data data, PoseStack poseStack, SubmitNodeCollector collector,
      int packedLight, int packedOverlay) {
    if (data == null || !data.present) {
      return;
    }
    if (data.clampAxis != null) {
      drawClamp(poseStack, collector, packedLight, packedOverlay, data.clamp, data.clampAxis, 0.0F, 0.0F, 0.0F);
    }

    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.rotate(data.orient);

    poseStack.translate(0.0, -0.5, 0.0);
    poseStack.scale(1.0F, data.stretch, 1.0F);
    poseStack.translate(0.0, 0.5, 0.0);

    data.support.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);
    poseStack.popPose();
  }

  private static void extractClamp(BlockEntity blockEntity, Data data) {
    BlockState state = blockEntity.getBlockState();

    java.util.List<Direction> connected = new java.util.ArrayList<>();
    for (Direction direction : Direction.values()) {
      if (connected(state, direction)) {
        connected.add(direction);
      }
    }
    if (connected.size() != 2 || connected.get(0) != connected.get(1).getOpposite()) {
      return;
    }
    data.clamp = Minecraft.getInstance().getModelManager().getStandaloneModel(CLAMP_KEY);
    data.clampAxis = connected.get(0).getAxis();
  }

  private static void drawClamp(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
      int packedOverlay, @Nullable BlockStateModelPart model, Direction.Axis axis,
      float offsetX, float offsetY, float offsetZ) {
    poseStack.pushPose();
    poseStack.translate(0.5 + offsetX, 0.5 + offsetY, 0.5 + offsetZ);
    switch (axis) {
      case X -> poseStack.rotateDegrees(Axis.YP, 90.0F);
      case Y -> poseStack.rotateDegrees(Axis.XP, 90.0F);
      default -> {
      }
    }
    poseStack.translate(-0.5, -0.5, -0.5);
    RenderStates.submitPart(collector, poseStack, model, packedLight, packedOverlay);
    poseStack.popPose();
  }

  private static boolean connected(BlockState state, Direction direction) {
    var property = VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction);
    return state.hasProperty(property) && state.getValue(property);
  }
}
