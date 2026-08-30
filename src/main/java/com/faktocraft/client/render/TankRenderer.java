package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.faktocraft.common.util.SpriteUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class TankRenderer implements BlockEntityRenderer<BlockEntityTank> {

  private static final float X0 = 2.5f / 16.0f;
  private static final float X1 = 13.5f / 16.0f;
  private static final float Y0 = 0.5f / 16.0f;
  private static final float Y_MAX = 15.5f / 16.0f;

  @Override
  public void render(BlockEntityTank tank, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
    if (tank.tank.isEmpty()) {
      return;
    }
    float fillFraction = (float) tank.tank.getFluidAmount() / tank.tank.getCapacityMb();
    if (fillFraction <= 0.001f) {
      return;
    }
    Fluid fluid = tank.tank.getFluid();
    TextureAtlasSprite sprite = SpriteUtil.getFluidSprite(fluid);
    if (sprite == null) {
      return;
    }
    int color = IClientFluidTypeExtensions.of(fluid).getTintColor() | 0xFF000000;

    boolean seamDown = false;
    boolean seamUp = false;
    var level = tank.getLevel();
    if (level != null) {
      seamDown = level.getBlockEntity(tank.getBlockPos().below()) instanceof BlockEntityTank below
          && !below.tank.isEmpty() && below.tank.getFluid().isSame(fluid)
          && below.tank.getFluidAmount() >= below.tank.getCapacityMb();
      seamUp = fillFraction >= 0.999f
          && level.getBlockEntity(tank.getBlockPos().above()) instanceof BlockEntityTank above
          && !above.tank.isEmpty() && above.tank.getFluid().isSame(fluid);
    }
    float y0 = seamDown ? 0.0f : Y0;
    float topLimit = seamUp ? 1.0f : Y_MAX;
    float top = y0 + (topLimit - y0) * Math.min(1.0f, fillFraction);

    VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
    CuboidRenderer.drawBox(poseStack.last(), vc, sprite, color, packedLight, X0, y0, X0, X1, top, X1,
        !seamDown, !seamUp);
  }
}
