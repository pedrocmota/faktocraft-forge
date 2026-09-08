package com.faktocraft.client.render;

import com.faktocraft.common.cover.DrillOps;
import com.faktocraft.common.item.impl.tools.HoleDrill;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

public final class DrillCrackOverlay {

  private record Crack(BlockPos pos, Direction face, int stage) {
  }

  private static final int STAGES = ModelBakery.DESTROY_TYPES.size();
  private static List<Crack> cracks = List.of();

  private DrillCrackOverlay() {
  }

  public static void tick(Minecraft minecraft) {
    if (minecraft.level == null) {
      cracks = List.of();
      return;
    }
    List<Crack> found = new ArrayList<>();
    int total = HoleDrill.drillTicks();
    for (Player player : minecraft.level.players()) {
      if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof HoleDrill)) {
        continue;
      }
      BlockHitResult hit = HoleDrill.target(minecraft.level, player);
      if (hit == null) {
        continue;
      }
      BlockPos pos = hit.getBlockPos();
      if (!DrillOps.canDrill(minecraft.level, pos, minecraft.level.getBlockState(pos), player, hit.getDirection())) {
        continue;
      }
      int elapsed = total - player.getUseItemRemainingTicks();
      int stage = Mth.clamp(elapsed * STAGES / Math.max(1, total), 0, STAGES - 1);
      found.add(new Crack(pos, hit.getDirection(), stage));
    }
    cracks = found;
  }

  public static void render(RenderLevelStageEvent event) {
    if (cracks.isEmpty() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null) {
      return;
    }
    Vec3 camera = event.getCamera().getPosition();
    PoseStack poseStack = event.getPoseStack();
    MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
    for (Crack crack : cracks) {
      RenderType type = ModelBakery.DESTROY_TYPES.get(crack.stage());
      poseStack.pushPose();
      poseStack.translate(crack.pos().getX() - camera.x, crack.pos().getY() - camera.y,
          crack.pos().getZ() - camera.z);
      int light = LevelRenderer.getLightColor(minecraft.level, crack.pos().relative(crack.face()));
      face(buffers.getBuffer(type), poseStack.last().pose(), poseStack.last().normal(), crack.face(), light);
      poseStack.popPose();
      buffers.endBatch(type);
    }
  }

  private static void face(VertexConsumer buffer, Matrix4f pose, Matrix3f normal, Direction face, int light) {
    Vector3f n = face.step();
    Vector3f tangent = face.getAxis() == Direction.Axis.Y ? new Vector3f(1F, 0F, 0F) : new Vector3f(0F, 1F, 0F);
    Vector3f bitangent = new Vector3f(n).cross(tangent);
    Vector3f center = new Vector3f(0.5F, 0.5F, 0.5F).add(new Vector3f(n).mul(0.5F));
    Vector3f[] corners = {
        corner(center, tangent, bitangent, -0.5F, -0.5F),
        corner(center, tangent, bitangent, 0.5F, -0.5F),
        corner(center, tangent, bitangent, 0.5F, 0.5F),
        corner(center, tangent, bitangent, -0.5F, 0.5F),
    };
    float[][] uvs = { { 0F, 0F }, { 1F, 0F }, { 1F, 1F }, { 0F, 1F } };
    for (int i = 0; i < corners.length; i++) {
      buffer.vertex(pose, corners[i].x, corners[i].y, corners[i].z)
          .color(255, 255, 255, 255)
          .uv(uvs[i][0], uvs[i][1])
          .uv2(light)
          .normal(normal, n.x, n.y, n.z)
          .endVertex();
    }
  }

  private static Vector3f corner(Vector3f center, Vector3f tangent, Vector3f bitangent, float u, float v) {
    return new Vector3f(center)
        .add(new Vector3f(tangent).mul(u))
        .add(new Vector3f(bitangent).mul(v));
  }
}
