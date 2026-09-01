package com.faktocraft.common.block.impl.generators.solar_panels;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.active.GuiSolarActive;
import com.faktocraft.common.screen.text.GuiTextSolar;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenSolarGenerator extends BetterScreen<MenuSolarGenerator> {

  public ScreenSolarGenerator(MenuSolarGenerator container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntitySolarGenerator be = (BlockEntitySolarGenerator) getBlockEntity();

    addRenderableOnlyComponent(new GuiSolarActive(this, 80, 25, be::getActive));
    addRenderableOnlyComponent(new GuiTextSolar(this, 60, 18, 88, 47, () -> be.amount));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/solar_generator.png");
  }
}
