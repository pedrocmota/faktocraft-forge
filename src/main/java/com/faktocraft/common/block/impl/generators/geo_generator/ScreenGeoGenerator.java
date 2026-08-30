package com.faktocraft.common.block.impl.generators.geo_generator;

import com.faktocraft.IndReb;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVerticalLarge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenGeoGenerator extends BetterScreen<MenuGeoGenerator> {

  public ScreenGeoGenerator(MenuGeoGenerator container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityGeoGenerator be = (BlockEntityGeoGenerator) getBlockEntity();

    addRenderableOnlyComponent(new GuiFluidBarVerticalLarge(this, 70, 19, be.fluidStorage));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/geo_generator.png");
  }
}
