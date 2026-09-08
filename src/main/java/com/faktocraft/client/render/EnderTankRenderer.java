package com.faktocraft.client.render;

import com.faktocraft.common.block.impl.pipe.BlockEntityEnderTank;
import com.faktocraft.common.util.SpriteUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class EnderTankRenderer implements BlockEntityRenderer<BlockEntityEnderTank> {

  private static final float X0 = 3.5f / 16.0f;
  private static final float X1 = 12.5f / 16.0f;
  private static final float Y0 = 2.0f / 16.0f;
  private static final float Y1 = 14.0f / 16.0f;
  private static final float FRONT_OFFSET = 0.46875f + 0.002f;
  private static final float PANEL_CENTER_Y = 11.0f / 16.0f;
  private static final float TEXT_SCALE = 0.014f;
  private static final int TEXT_COLOR = 0xD8B4FF;

  private final Font font;

  public EnderTankRenderer(BlockEntityRendererProvider.Context context) {
    this.font = context.getFont();
  }

  @Override
  public void render(BlockEntityEnderTank tank, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight, int packedOverlay) {
    renderFluid(tank, poseStack, buffer, packedLight);
    if (!tank.hasCode() || !tank.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      return;
    }
    Direction facing = tank.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    String text = tank.codeText();
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
    poseStack.translate(0.0f, PANEL_CENTER_Y - 0.5f, FRONT_OFFSET);
    poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
    font.drawInBatch(text, -font.width(text) / 2.0f, -4.0f, TEXT_COLOR, false, poseStack.last().pose(), buffer,
        Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    poseStack.popPose();
  }

  private static void renderFluid(BlockEntityEnderTank tank, PoseStack poseStack, MultiBufferSource buffer,
      int packedLight) {
    FluidStack fluid = tank.view.getFluidStack();
    if (fluid.isEmpty()) {
      return;
    }
    float fillFraction = Math.min(1.0f, (float) fluid.getAmount() / tank.view.getCapacityMb());
    if (fillFraction <= 0.001f) {
      return;
    }
    TextureAtlasSprite sprite = SpriteUtil.getFluidSprite(fluid.getFluid());
    if (sprite == null) {
      return;
    }
    int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid) | 0xFF000000;
    VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
    CuboidRenderer.drawBox(poseStack.last(), vc, sprite, color, packedLight, X0, Y0, X0, X1,
        Y0 + (Y1 - Y0) * fillFraction, X1);
  }
}
