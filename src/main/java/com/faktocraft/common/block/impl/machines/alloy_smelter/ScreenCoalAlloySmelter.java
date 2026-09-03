package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import com.faktocraft.common.screen.progress.GuiProgressFuel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCoalAlloySmelter extends BetterScreen<MenuCoalAlloySmelter> {

  public ScreenCoalAlloySmelter(MenuCoalAlloySmelter container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityCoalAlloySmelter be = (BlockEntityCoalAlloySmelter) getBlockEntity();

    addRenderableOnlyComponent(new GuiProgressFuel(this, 152, 42, be.fuel));
    addRenderableOnlyComponent(new GuiProgressArrow(this, 81, 33, be.progress));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/coal_alloy_smelter.png");
  }
}
