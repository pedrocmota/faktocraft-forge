package com.faktocraft.mixin.client;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(RegisterFluidModelsEvent.class)
public interface RegisterFluidModelsEventAccessor {
  @Accessor(value = "models", remap = false)
  Map<Fluid, FluidModel> faktocraftModels();
}
