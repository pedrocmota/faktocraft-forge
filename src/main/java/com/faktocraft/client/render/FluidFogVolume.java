package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.fluids.FluidType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;

public final class FluidFogVolume {

  private static final ResourceLocation WHITE = new ResourceLocation(Faktocraft.MODID, "textures/misc/white.png");
  private static final int LAYERS = 16;
  private static final float QUAD_SPREAD = 3.0F;
  private static final Map<FluidType, float[]> FOGS = new HashMap<>();

  private FluidFogVolume() {
  }

  public static void register(FluidType type, float red, float green, float blue, float end) {
    FOGS.put(type, new float[] { red, green, blue, end });
  }

  public static void render(RenderLevelStageEvent event) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !ShaderPacks.inUse()) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null) {
      return;
    }
    Camera camera = event.getCamera();
    BlockPos eye = camera.getBlockPosition();
    FluidState fluid = minecraft.level.getFluidState(eye);
    float[] fog = fluid.isEmpty() ? null : FOGS.get(fluid.getFluidType());
    if (fog == null || camera.getPosition().y > eye.getY() + fluid.getHeight(minecraft.level, eye)) {
      return;
    }
    float brightness = LightTexture.getBrightness(minecraft.level.dimensionType(),
        minecraft.level.getMaxLocalRawBrightness(eye));
    int red = (int) (fog[0] * brightness * 255.0F);
    int green = (int) (fog[1] * brightness * 255.0F);
    int blue = (int) (fog[2] * brightness * 255.0F);

    Vector3f look = camera.getLookVector();
    Vector3f up = camera.getUpVector();
    Vector3f left = camera.getLeftVector();
    PoseStack poseStack = event.getPoseStack();
    Matrix4f matrix = poseStack.last().pose();
    MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
    VertexConsumer buffer = buffers.getBuffer(RenderType.beaconBeam(WHITE, true));
    for (int i = LAYERS; i >= 1; i--) {
      float distance = fog[3] * i / LAYERS;
      int alpha = (int) (255.0F / (LAYERS - i + 1));
      float spread = distance * QUAD_SPREAD;
      float cx = look.x * distance;
      float cy = look.y * distance;
      float cz = look.z * distance;
      float ux = up.x * spread;
      float uy = up.y * spread;
      float uz = up.z * spread;
      float lx = left.x * spread;
      float ly = left.y * spread;
      float lz = left.z * spread;
      vertex(buffer, matrix, cx + lx - ux, cy + ly - uy, cz + lz - uz, red, green, blue, alpha);
      vertex(buffer, matrix, cx - lx - ux, cy - ly - uy, cz - lz - uz, red, green, blue, alpha);
      vertex(buffer, matrix, cx - lx + ux, cy - ly + uy, cz - lz + uz, red, green, blue, alpha);
      vertex(buffer, matrix, cx + lx + ux, cy + ly + uy, cz + lz + uz, red, green, blue, alpha);
      vertex(buffer, matrix, cx + lx + ux, cy + ly + uy, cz + lz + uz, red, green, blue, alpha);
      vertex(buffer, matrix, cx - lx + ux, cy - ly + uy, cz - lz + uz, red, green, blue, alpha);
      vertex(buffer, matrix, cx - lx - ux, cy - ly - uy, cz - lz - uz, red, green, blue, alpha);
      vertex(buffer, matrix, cx + lx - ux, cy + ly - uy, cz + lz - uz, red, green, blue, alpha);
    }
    buffers.endBatch();
  }

  private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z,
      int red, int green, int blue, int alpha) {
    buffer.vertex(matrix, x, y, z)
        .color(red, green, blue, alpha)
        .uv(0.0F, 0.0F)
        .overlayCoords(OverlayTexture.NO_OVERLAY)
        .uv2(LightTexture.FULL_BRIGHT)
        .normal(0.0F, 1.0F, 0.0F)
        .endVertex();
  }
}
