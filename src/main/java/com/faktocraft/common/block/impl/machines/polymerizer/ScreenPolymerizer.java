package com.faktocraft.common.block.impl.machines.polymerizer;

import com.faktocraft.IndReb;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.progress.GuiProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenPolymerizer extends BetterScreen<MenuPolymerizer> {

  public ScreenPolymerizer(MenuPolymerizer container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityPolymerizer be = (BlockEntityPolymerizer) getBlockEntity();

    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 8, 18, be.oilStorage));
    addRenderableOnlyComponent(new GuiProgress(this, 88, 35, be.progress, GuiSprite.ARROW,
        GuiProgress.Direction.HORIZONTAL, false));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/polymerizer.png");
  }
}
