package com.faktocraft.common.block.impl.machines.distillery;

import com.faktocraft.IndReb;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenDistillery extends BetterScreen<MenuDistillery> {

  public ScreenDistillery(MenuDistillery container, Inventory inv, Component name) {
    super(container, inv, name, 176, 188);
    this.inventoryLabelY = 94;
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityDistillery be = (BlockEntityDistillery) getBlockEntity();

    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 26, 25, be.oilTank));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 48, 25, be.acidTank));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 70, 25, be.waterTank));
    addRenderableOnlyComponent(new GuiProgressArrow(this, 94, 43, be.progress));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 120, 25, be.fuelTank));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/distillery.png");
  }
}
