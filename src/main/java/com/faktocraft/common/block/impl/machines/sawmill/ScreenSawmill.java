package com.faktocraft.common.block.impl.machines.sawmill;

import com.faktocraft.IndReb;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressSawing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenSawmill extends BetterScreen<MenuSawmill> {

  public ScreenSawmill(MenuSawmill container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    addRenderableOnlyComponent(new GuiProgressSawing(this, 71, 35, ((BlockEntitySawmill) getBlockEntity()).progress));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/standard_machine.png");
  }
}
