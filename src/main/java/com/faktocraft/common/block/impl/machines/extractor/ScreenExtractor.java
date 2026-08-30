package com.faktocraft.common.block.impl.machines.extractor;

import com.faktocraft.IndReb;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressExtracting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenExtractor extends BetterScreen<MenuExtractor> {

  public ScreenExtractor(MenuExtractor container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    addRenderableOnlyComponent(
        new GuiProgressExtracting(this, 71, 35, ((BlockEntityExtractor) getBlockEntity()).progress));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/standard_machine.png");
  }
}
