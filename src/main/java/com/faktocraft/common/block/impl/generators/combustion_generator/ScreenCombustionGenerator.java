package com.faktocraft.common.block.impl.generators.combustion_generator;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVerticalLarge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCombustionGenerator extends BetterScreen<MenuCombustionGenerator> {

  public ScreenCombustionGenerator(MenuCombustionGenerator container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityCombustionGenerator be = (BlockEntityCombustionGenerator) getBlockEntity();

    addRenderableOnlyComponent(new GuiFluidBarVerticalLarge(this, 70, 19, be.fluidStorage));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/combustion_generator.png");
  }
}
