package com.faktocraft.integration.jei;

import com.faktocraft.common.screen.PanelScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;
import java.util.List;

public class GuiHandler implements IGuiContainerHandler<PanelScreen<?>> {

  @Override
  public List<Rect2i> getGuiExtraAreas(PanelScreen<?> containerScreen) {
    return containerScreen.getAreas();
  }
}
