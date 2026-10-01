package com.faktocraft.client.render;

import com.faktocraft.common.util.SpriteUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class FluidSprites {
  private FluidSprites() {
  }

  @Nullable
  public static FluidModel model(Fluid fluid) {
    if (fluid == null || fluid == Fluids.EMPTY) {
      return null;
    }
    return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
  }

  @Nullable
  public static TextureAtlasSprite still(Fluid fluid) {
    FluidModel model = model(fluid);
    return model == null ? null : model.stillMaterial().sprite();
  }

  @Nullable
  public static TextureAtlasSprite still(FluidStack stack) {
    return stack.isEmpty() ? null : still(stack.getFluid());
  }

  public static int tint(Fluid fluid) {
    FluidModel model = model(fluid);
    if (model == null || model.fluidTintSource() == null) {
      return -1;
    }
    return model.fluidTintSource().color(fluid.defaultFluidState());
  }

  public static int tint(FluidStack stack) {
    FluidModel model = stack.isEmpty() ? null : model(stack.getFluid());
    if (model == null || model.fluidTintSource() == null) {
      return -1;
    }
    return model.fluidTintSource().colorAsStack(stack);
  }

  public static int tint(FluidState state) {
    return tint(state.getType());
  }

  public static TextureAtlasSprite block(Identifier texture) {
    return Minecraft.getInstance().getAtlasManager().get(new SpriteId(SpriteUtil.blockAtlas(), texture));
  }
}
