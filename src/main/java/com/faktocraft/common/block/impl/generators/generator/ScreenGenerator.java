package com.faktocraft.common.block.impl.generators.generator;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressFuel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenGenerator extends BetterScreen<MenuGenerator> {

  public ScreenGenerator(MenuGenerator container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    addRenderableOnlyComponent(
        new GuiProgressFuel(this, 80, 57, ((BlockEntityGenerator) getBlockEntity()).progressBurn));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/generator.png");
  }
}
