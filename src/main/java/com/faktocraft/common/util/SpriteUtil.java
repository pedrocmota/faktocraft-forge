package com.faktocraft.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class SpriteUtil {

  public static TextureAtlasSprite getFluidSprite(FluidStack stack) {
    ResourceLocation still = IClientFluidTypeExtensions.of(stack.getFluid()).getStillTexture(stack);
    return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
  }

  public static TextureAtlasSprite getFluidSprite(Fluid fluid) {
    ResourceLocation still = IClientFluidTypeExtensions.of(fluid).getStillTexture();
    return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
  }
}
