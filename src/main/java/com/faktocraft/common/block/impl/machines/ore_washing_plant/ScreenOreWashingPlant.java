package com.faktocraft.common.block.impl.machines.ore_washing_plant;

import com.faktocraft.IndReb;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.progress.GuiProgressOreWashing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenOreWashingPlant extends BetterScreen<MenuOreWashingPlant> {

  public ScreenOreWashingPlant(MenuOreWashingPlant container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityOreWashingPlant be = (BlockEntityOreWashingPlant) getBlockEntity();

    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 8, 18, be.waterStorage));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 30, 18, be.acidStorage));
    addRenderableOnlyComponent(new GuiProgressOreWashing(this, 88, 33, be.progress));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/ore_washing_plant.png");
  }
}
