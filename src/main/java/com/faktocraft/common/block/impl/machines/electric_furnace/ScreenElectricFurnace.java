package com.faktocraft.common.block.impl.machines.electric_furnace;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenElectricFurnace extends BetterScreen<MenuElectricFurnace> {

  public ScreenElectricFurnace(MenuElectricFurnace container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    addRenderableOnlyComponent(
        new GuiProgressArrow(this, 71, 35, ((BlockEntityElectricFurnace) getBlockEntity()).progress));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/electric_furnace.png");
  }
}
