package com.faktocraft.common.util;

import net.minecraft.resources.Identifier;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class SpriteUtil {
  public static Identifier blockAtlas() {
    return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).location();
  }

  public static float getU(TextureAtlasSprite sprite, float pixels) {
    return sprite.getU(pixels / 16.0F);
  }

  public static float getV(TextureAtlasSprite sprite, float pixels) {
    return sprite.getV(pixels / 16.0F);
  }

  @Nullable
  public static FluidModel getFluidModel(Fluid fluid) {
    if (fluid == null || fluid == Fluids.EMPTY) {
      return null;
    }
    return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
  }

  @Nullable
  public static TextureAtlasSprite getFluidSprite(FluidStack stack) {
    return stack == null || stack.isEmpty() ? null : getFluidSprite(stack.getFluid());
  }

  @Nullable
  public static TextureAtlasSprite getFluidSprite(Fluid fluid) {
    FluidModel model = getFluidModel(fluid);
    return model != null ? model.stillMaterial().sprite() : null;
  }

  public static int getFluidTint(FluidStack stack) {
    if (stack == null || stack.isEmpty()) {
      return -1;
    }
    FluidModel model = getFluidModel(stack.getFluid());
    return model != null && model.fluidTintSource() != null ? model.fluidTintSource().colorAsStack(stack) : -1;
  }

  public static int getFluidTint(Fluid fluid) {
    FluidModel model = getFluidModel(fluid);
    return model != null && model.fluidTintSource() != null
        ? model.fluidTintSource().color(fluid.defaultFluidState())
        : -1;
  }
}
