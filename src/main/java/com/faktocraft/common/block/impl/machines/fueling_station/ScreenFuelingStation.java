package com.faktocraft.common.block.impl.machines.fueling_station;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenFuelingStation extends BetterScreen<MenuFuelingStation> {

  public ScreenFuelingStation(MenuFuelingStation container, Inventory inv, Component name) {
    super(container, inv, name, 176, 188);
    this.inventoryLabelY = 94;
  }

  @Override
  protected void init() {
    super.init();
    BlockEntityFuelingStation be = (BlockEntityFuelingStation) getBlockEntity();
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 56, 30, be.tank));
    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/fueling_station.png");
  }
}
