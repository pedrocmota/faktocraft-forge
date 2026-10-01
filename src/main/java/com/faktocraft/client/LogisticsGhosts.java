package com.faktocraft.client;

import com.faktocraft.client.render.RenderStates;
import com.faktocraft.common.network.packet.PacketLogisticsGhost;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class LogisticsGhosts {
  private static final float SCALE = 0.35f;

  private static final class Ghost {
    final List<BlockPos> path;
    final ItemStack stack;
    final int duration;
    final ItemStackRenderState renderState = new ItemStackRenderState();

    final float[] arrival;
    int age;

    Ghost(List<BlockPos> path, ItemStack stack, int duration, int[] hopTicks) {
      this.path = path;
      this.stack = stack;
      this.duration = Math.max(2, duration);
      int segments = path.size() - 1;
      this.arrival = new float[segments + 1];
      boolean weighted = hopTicks.length == segments;
      int total = 0;
      for (int i = 0; i < segments; i++) {
        total += weighted ? Math.max(1, hopTicks[i]) : 1;
        arrival[i + 1] = total;
      }
      for (int i = 1; i <= segments; i++) {
        arrival[i] /= total;
      }
    }

    Vec3 positionAt(float partialTick) {
      float progress = Math.min(1.0f, (age + partialTick) / duration);
      int index = 0;
      int high = arrival.length - 2;
      while (index < high) {
        int mid = (index + high + 1) / 2;
        if (arrival[mid] <= progress) {
          index = mid;
        } else {
          high = mid - 1;
        }
      }
      float span = arrival[index + 1] - arrival[index];
      float local = span > 0 ? Math.min(1.0f, (progress - arrival[index]) / span) : 1.0f;
      Vec3 from = Vec3.atCenterOf(path.get(index));
      Vec3 to = Vec3.atCenterOf(path.get(index + 1));
      return from.lerp(to, local);
    }
  }

  private static final List<Ghost> GHOSTS = new ArrayList<>();

  private LogisticsGhosts() {
  }

  public static void add(PacketLogisticsGhost packet) {
    if (packet.path().size() >= 2 && !packet.stack().isEmpty()) {
      GHOSTS.add(new Ghost(packet.path(), packet.stack(), packet.durationTicks(), packet.hopTicks()));
    }
  }

  public static void tick(Minecraft minecraft) {
    if (minecraft.level == null) {
      GHOSTS.clear();
      return;
    }
    if (minecraft.isPaused()) {
      return;
    }
    Iterator<Ghost> iterator = GHOSTS.iterator();
    while (iterator.hasNext()) {
      Ghost ghost = iterator.next();
      if (++ghost.age >= ghost.duration) {
        iterator.remove();
      }
    }
  }

  public static void submit(SubmitCustomGeometryEvent event) {
    if (GHOSTS.isEmpty()) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null) {
      return;
    }
    Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
    PoseStack poseStack = event.getPoseStack();
    float partialTick = event.getLevelRenderState().worldPartialTicks;

    for (Ghost ghost : GHOSTS) {
      Vec3 pos = ghost.positionAt(partialTick);
      poseStack.pushPose();
      poseStack.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
      poseStack.rotateDegrees(Axis.YP, (ghost.age + partialTick) * 6.0f);
      poseStack.scale(SCALE, SCALE, SCALE);
      int light = LightCoordsUtil.getLightCoords(minecraft.level, BlockPos.containing(pos.x, pos.y, pos.z));
      RenderStates.item(ghost.renderState, ghost.stack, minecraft.level, ItemDisplayContext.FIXED);
      ghost.renderState.submit(poseStack, event.getSubmitNodeCollector(), light, OverlayTexture.NO_OVERLAY, 0);
      poseStack.popPose();
    }
  }
}
