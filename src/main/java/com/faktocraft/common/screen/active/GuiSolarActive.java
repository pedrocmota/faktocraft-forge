package com.faktocraft.common.screen.active;

import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.util.Constants;
import net.minecraft.resources.ResourceLocation;
import java.util.function.BooleanSupplier;

public class GuiSolarActive extends GuiActive {

  public GuiSolarActive(IGuiWrapper wrapper, int leftOffset, int topOffset, boolean active) {
    super(wrapper, GuiSprite.SOLAR_SUN, leftOffset, topOffset, active);
  }

  public GuiSolarActive(IGuiWrapper wrapper, int leftOffset, int topOffset, BooleanSupplier active) {
    super(wrapper, GuiSprite.SOLAR_SUN, leftOffset, topOffset, active);
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.PROCESS;
  }
}
