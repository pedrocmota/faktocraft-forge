package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3fc;
import java.util.HashMap;
import java.util.Map;

public final class FluidFogVolume {
  private static final Identifier WHITE = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/misc/white.png");
  private static final int LAYERS = 16;
  private static final float QUAD_SPREAD = 3.0F;
  private static final Map<FluidType, float[]> FOGS = new HashMap<>();

  private FluidFogVolume() {
  }

  public static void register(FluidType type, float red, float green, float blue, float end) {
    FOGS.put(type, new float[] { red, green, blue, end });
  }

  public static void submit(SubmitCustomGeometryEvent event) {
    if (!ShaderPacks.inUse()) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null) {
      return;
    }
    Camera camera = minecraft.gameRenderer.mainCamera();
    BlockPos eye = camera.blockPosition();
    FluidState fluid = minecraft.level.getFluidState(eye);
    float[] fog = fluid.isEmpty() ? null : FOGS.get(fluid.getFluidType());
    if (fog == null || camera.position().y > eye.getY() + fluid.getHeight(minecraft.level, eye)) {
      return;
    }
    float brightness = Lightmap.getBrightness(minecraft.level.dimensionType(),
        minecraft.level.getMaxLocalRawBrightness(eye));
    int red = (int) (fog[0] * brightness * 255.0F);
    int green = (int) (fog[1] * brightness * 255.0F);
    int blue = (int) (fog[2] * brightness * 255.0F);
    float end = fog[3];

    Vector3fc look = camera.forwardVector();
    Vector3fc up = camera.upVector();
    Vector3fc left = camera.leftVector();
    PoseStack poseStack = event.getPoseStack();
    event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.beaconBeam(WHITE, true),
        (pose, buffer) -> {
          for (int i = LAYERS; i >= 1; i--) {
            float distance = end * i / LAYERS;
            int alpha = (int) (255.0F / (LAYERS - i + 1));
            float spread = distance * QUAD_SPREAD;
            float cx = look.x() * distance;
            float cy = look.y() * distance;
            float cz = look.z() * distance;
            float ux = up.x() * spread;
            float uy = up.y() * spread;
            float uz = up.z() * spread;
            float lx = left.x() * spread;
            float ly = left.y() * spread;
            float lz = left.z() * spread;
            vertex(buffer, pose, cx + lx - ux, cy + ly - uy, cz + lz - uz, red, green, blue, alpha);
            vertex(buffer, pose, cx - lx - ux, cy - ly - uy, cz - lz - uz, red, green, blue, alpha);
            vertex(buffer, pose, cx - lx + ux, cy - ly + uy, cz - lz + uz, red, green, blue, alpha);
            vertex(buffer, pose, cx + lx + ux, cy + ly + uy, cz + lz + uz, red, green, blue, alpha);
            vertex(buffer, pose, cx + lx + ux, cy + ly + uy, cz + lz + uz, red, green, blue, alpha);
            vertex(buffer, pose, cx - lx + ux, cy - ly + uy, cz - lz + uz, red, green, blue, alpha);
            vertex(buffer, pose, cx - lx - ux, cy - ly - uy, cz - lz - uz, red, green, blue, alpha);
            vertex(buffer, pose, cx + lx - ux, cy + ly - uy, cz + lz - uz, red, green, blue, alpha);
          }
        });
  }

  private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z,
      int red, int green, int blue, int alpha) {
    buffer.addVertex(pose, x, y, z)
        .setColor(red, green, blue, alpha)
        .setUv(0.0F, 0.0F)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(LightCoordsUtil.FULL_BRIGHT)
        .setNormal(pose, 0.0F, 1.0F, 0.0F);
  }
}
