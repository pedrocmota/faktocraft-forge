package com.faktocraft.common.block.impl.machines.iron_furnace;

import com.faktocraft.IndReb;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import com.faktocraft.common.screen.progress.GuiProgressFuel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenIronFurnace extends BetterScreen<MenuIronFurnace> {

  public ScreenIronFurnace(MenuIronFurnace container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    addRenderableOnlyComponent(
        new GuiProgressArrow(this, 79, 35, ((BlockEntityIronFurnace) getBlockEntity()).smelting));
    addRenderableOnlyComponent(new GuiProgressFuel(this, 56, 35, ((BlockEntityIronFurnace) getBlockEntity()).fuel));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/iron_furnace.png");
  }
}
