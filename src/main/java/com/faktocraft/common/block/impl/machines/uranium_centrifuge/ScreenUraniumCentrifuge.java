package com.faktocraft.common.block.impl.machines.uranium_centrifuge;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenUraniumCentrifuge extends BetterScreen<MenuUraniumCentrifuge> {

  public ScreenUraniumCentrifuge(MenuUraniumCentrifuge container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    addRenderableOnlyComponent(new GuiProgressArrow(this, 71, 35,
        ((BlockEntityUraniumCentrifuge) getBlockEntity()).progress));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/standard_machine.png");
  }
}
