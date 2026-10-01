package com.faktocraft.common.block.impl.machines.thermal_centrifuge;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import com.faktocraft.common.screen.text.GuiTextTemperature;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenThermalCentrifuge extends BetterScreen<MenuThermalCentrifuge> {

  public ScreenThermalCentrifuge(MenuThermalCentrifuge container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityThermalCentrifuge be = (BlockEntityThermalCentrifuge) getBlockEntity();

    addRenderableOnlyComponent(new GuiProgressArrow(this, 73, 33, be.progress));
    addRenderableOnlyComponent(new GuiTextTemperature(this, 55, 5, 38, 59, be.tempLevel));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/thermal_centrifuge.png");
  }
}
