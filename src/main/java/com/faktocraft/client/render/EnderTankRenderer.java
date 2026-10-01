package com.faktocraft.client.render;

import com.faktocraft.common.util.SpriteUtil;
import com.faktocraft.common.block.impl.pipe.BlockEntityEnderTank;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class EnderTankRenderer implements BlockEntityRenderer<BlockEntityEnderTank, EnderTankRenderer.State> {

  private static final float X0 = 3.5f / 16.0f;
  private static final float X1 = 12.5f / 16.0f;
  private static final float Y0 = 2.0f / 16.0f;
  private static final float Y1 = 14.0f / 16.0f;
  private static final float FRONT_OFFSET = 0.46875f + 0.002f;
  private static final float PANEL_CENTER_Y = 11.0f / 16.0f;
  private static final float TEXT_SCALE = 0.014f;
  private static final int TEXT_COLOR = 0xFFD8B4FF;

  public static class State extends BlockEntityRenderState {
    @Nullable
    TextureAtlasSprite sprite;
    int color;
    float top;
    @Nullable
    String text;
    @Nullable
    Direction facing;
  }

  private final Font font;

  public EnderTankRenderer(BlockEntityRendererProvider.Context context) {
    this.font = context.font();
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(BlockEntityEnderTank tank, State state, float partialTick, Vec3 cameraPos,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(tank, state, partialTick, cameraPos, breakProgress);
    state.sprite = null;
    state.text = null;
    FluidStack fluid = tank.view.getFluidStack();
    if (!fluid.isEmpty()) {
      float fillFraction = Math.min(1.0f, (float) fluid.getAmount() / tank.view.getCapacityMb());
      if (fillFraction > 0.001f) {
        TextureAtlasSprite sprite = FluidSprites.still(fluid.getFluid());
        if (sprite != null) {
          state.sprite = sprite;
          state.color = FluidSprites.tint(fluid) | 0xFF000000;
          state.top = Y0 + (Y1 - Y0) * fillFraction;
        }
      }
    }
    if (tank.hasCode() && tank.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      state.facing = tank.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
      state.text = tank.codeText();
    }
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    TextureAtlasSprite sprite = state.sprite;
    if (sprite != null) {
      int light = state.lightCoords;
      int color = state.color;
      float top = state.top;
      collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(SpriteUtil.blockAtlas()),
          (pose, vc) -> CuboidRenderer.drawBox(pose, vc, sprite, color, light, X0, Y0, X0, X1, top, X1));
    }
    if (state.text == null || state.facing == null) {
      return;
    }
    String text = state.text;
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.rotateDegrees(Axis.YP, -state.facing.toYRot());
    poseStack.translate(0.0f, PANEL_CENTER_Y - 0.5f, FRONT_OFFSET);
    poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
    collector.submitText(poseStack, -font.width(text) / 2.0f, -4.0f, FormattedCharSequence.forward(text, Style.EMPTY),
        false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, TEXT_COLOR, 0, 0);
    poseStack.popPose();
  }
}
