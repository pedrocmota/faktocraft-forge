package com.faktocraft.client.model;

import com.faktocraft.Faktocraft;
import com.faktocraft.client.render.FluidSprites;
import com.faktocraft.common.registries.ModComponentsFluids;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FluidTintSource implements ItemTintSource {
  public static final Identifier ID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "fluid");
  public static final FluidTintSource INSTANCE = new FluidTintSource();
  public static final MapCodec<FluidTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

  private static final int NO_FLUID = 0xFFFFFFFF;
  private static final int LAVA_FALLBACK = 0xFFD45A12;

  @Override
  public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
    Identifier fluidId = ModComponentsFluids.getFluid(stack);
    Fluid fluid = fluidId == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.getValue(fluidId);
    if (fluid == null || fluid == Fluids.EMPTY) {
      return NO_FLUID;
    }

    int color = FluidSprites.tint(fluid);
    if ((color & 0x00FFFFFF) == 0x00FFFFFF) {
      if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
        return LAVA_FALLBACK;
      }
      return NO_FLUID;
    }
    return color | 0xFF000000;
  }

  @Override
  public MapCodec<? extends ItemTintSource> type() {
    return MAP_CODEC;
  }
}
