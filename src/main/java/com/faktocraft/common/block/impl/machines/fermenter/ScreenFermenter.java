package com.faktocraft.common.block.impl.machines.fermenter;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFertilizerBar;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.bar.GuiFluidBarVerticalLarge;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import com.faktocraft.common.screen.text.GuiTextHeat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenFermenter extends BetterScreen<MenuFermenter> {

  public ScreenFermenter(MenuFermenter container, Inventory inv, Component name) {
    super(container, inv, name, 176, 188);
    this.inventoryLabelY = 94;
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityFermenter be = (BlockEntityFermenter) getBlockEntity();

    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 108, 18, be.fluidOutputStorage));
    addRenderableOnlyComponent(new GuiProgressArrow(this, 76, 35, be.progress));
    addRenderableOnlyComponent(new GuiFertilizerBar(this, 12, 74, be.progressWaste));
    addRenderableOnlyComponent(new GuiFluidBarVerticalLarge(this, 32, 18, be.fluidInputStorage));
    addRenderableOnlyComponent(new GuiTextHeat(this, 20, 10, 88, 82, be.heatLevel));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/fermenter.png");
  }
}
