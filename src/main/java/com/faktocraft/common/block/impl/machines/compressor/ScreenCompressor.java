package com.faktocraft.common.block.impl.machines.compressor;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressCompressing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCompressor extends BetterScreen<MenuCompressor> {

  public ScreenCompressor(MenuCompressor container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    addRenderableOnlyComponent(
        new GuiProgressCompressing(this, 71, 35, ((BlockEntityCompressor) getBlockEntity()).progress));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/standard_machine.png");
  }
}
