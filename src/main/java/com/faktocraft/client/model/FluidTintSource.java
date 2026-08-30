package com.faktocraft.client.model;

import com.faktocraft.common.registries.ModComponentsFluids;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidTintSource implements ItemColor {

  public static final FluidTintSource INSTANCE = new FluidTintSource();

  private static final int NO_FLUID = 0xFFFFFFFF;
  private static final int LAVA_FALLBACK = 0xFFD45A12;

  @Override
  public int getColor(ItemStack stack, int tintIndex) {
    if (tintIndex != 1) {
      return NO_FLUID;
    }

    ResourceLocation fluidId = ModComponentsFluids.getFluid(stack);
    Fluid fluid = fluidId == null ? Fluids.EMPTY : ForgeRegistries.FLUIDS.getValue(fluidId);
    if (fluid == null || fluid == Fluids.EMPTY) {
      return NO_FLUID;
    }

    int color = IClientFluidTypeExtensions.of(fluid).getTintColor();
    if ((color & 0x00FFFFFF) == 0x00FFFFFF) {
      if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
        return LAVA_FALLBACK;
      }
      return NO_FLUID;
    }
    return color | 0xFF000000;
  }
}
