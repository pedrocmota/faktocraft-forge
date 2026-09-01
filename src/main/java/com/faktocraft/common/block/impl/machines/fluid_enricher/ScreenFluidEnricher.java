package com.faktocraft.common.block.impl.machines.fluid_enricher;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenFluidEnricher extends BetterScreen<MenuFluidEnricher> {

  public ScreenFluidEnricher(MenuFluidEnricher container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityFluidEnricher be = (BlockEntityFluidEnricher) getBlockEntity();

    addRenderableOnlyComponent(new GuiProgressArrow(this, 76, 35, be.progress));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 36, 18, be.fluidInputStorage));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 54, 18, be.fluidInputStorage2));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 108, 18, be.fluidOutputStorage));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/fluid_enricher.png");
  }
}
