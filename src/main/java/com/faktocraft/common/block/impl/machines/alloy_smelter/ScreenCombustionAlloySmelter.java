package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCombustionAlloySmelter extends BetterScreen<MenuCombustionAlloySmelter> {

  public ScreenCombustionAlloySmelter(MenuCombustionAlloySmelter container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityCombustionAlloySmelter be = (BlockEntityCombustionAlloySmelter) getBlockEntity();

    addRenderableOnlyComponent(new GuiProgressArrow(this, 81, 33, be.progress));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 150, 18, be.fluidStorage));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/combustion_alloy_smelter.png");
  }
}
