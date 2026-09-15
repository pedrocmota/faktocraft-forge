package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.bar.GuiHeatBar;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenNuclearReactor extends BetterScreen<MenuNuclearReactor> {

  public ScreenNuclearReactor(MenuNuclearReactor container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();
    if (getBlockEntity() instanceof BlockEntityNuclearReactor reactor) {
      addRenderableOnlyComponent(new GuiFluidBarVertical(this, 8, 17, reactor.water));
      addRenderableOnlyComponent(new GuiHeatBar(this, 112, 17, reactor));
    }
    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/nuclear_reactor.png");
  }
}
