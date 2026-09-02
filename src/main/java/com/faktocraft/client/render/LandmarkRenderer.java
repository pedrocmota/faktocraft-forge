package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.quarry.BlockEntityLandmark;
import com.faktocraft.common.block.impl.quarry.BlockLandmark;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

public class LandmarkRenderer implements BlockEntityRenderer<BlockEntityLandmark> {

  private static final ResourceLocation BEAM_SPRITE = new ResourceLocation(Faktocraft.MODID,
      "block/misc/landmark_beam");
  private static final int SCAN_INTERVAL_TICKS = 10;
  private static final int COLOR = 0xB45AC8FF;
  private static final float LOW = 7.25F / 16.0F;
  private static final float HIGH = 8.75F / 16.0F;

  @Override
  public void render(BlockEntityLandmark landmark, float partialTick, PoseStack poseStack,
      MultiBufferSource buffer, int packedLight, int packedOverlay) {
    Level level = landmark.getLevel();
    if (level == null) {
      return;
    }
    long now = level.getGameTime();
    if (now >= landmark.nextScanTime) {
      BlockPos pos = landmark.getBlockPos();
      landmark.eastPartner = BlockLandmark.findPartner(level, pos, Direction.EAST);
      landmark.southPartner = BlockLandmark.findPartner(level, pos, Direction.SOUTH);
      landmark.nextScanTime = now + SCAN_INTERVAL_TICKS;
    }
    if (landmark.eastPartner == null && landmark.southPartner == null) {
      return;
    }
    TextureAtlasSprite sprite = Minecraft.getInstance()
        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BEAM_SPRITE);
    if (sprite == null) {
      return;
    }
    VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
    PoseStack.Pose pose = poseStack.last();
    BlockPos pos = landmark.getBlockPos();
    if (landmark.eastPartner != null) {
      float end = landmark.eastPartner.getX() - pos.getX() + 0.5F;
      CuboidRenderer.drawBox(pose, vc, sprite, COLOR, LightTexture.FULL_BRIGHT,
          0.5F, LOW, LOW, end, HIGH, HIGH);
    }
    if (landmark.southPartner != null) {
      float end = landmark.southPartner.getZ() - pos.getZ() + 0.5F;
      CuboidRenderer.drawBox(pose, vc, sprite, COLOR, LightTexture.FULL_BRIGHT,
          LOW, LOW, 0.5F, HIGH, HIGH, end);
    }
  }

  @Override
  public boolean shouldRenderOffScreen(BlockEntityLandmark landmark) {
    return true;
  }

  @Override
  public int getViewDistance() {
    return BlockLandmark.MAX_SPAN * 2;
  }
}
