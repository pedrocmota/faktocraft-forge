package com.faktocraft.common.screen.active;

import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import java.util.function.BooleanSupplier;

public class GuiActive extends GuiElement {

  private final BooleanSupplier active;
  private final GuiSprite progressType;

  public GuiActive(IGuiWrapper wrapper, GuiSprite progressType, int leftOffset, int topOffset, boolean active) {
    this(wrapper, progressType, leftOffset, topOffset, () -> active);
  }

  public GuiActive(IGuiWrapper wrapper, GuiSprite progressType, int leftOffset, int topOffset, BooleanSupplier active) {
    super(wrapper, progressType.getWidth(), progressType.getHeight(), leftOffset, topOffset);
    this.active = active;
    this.progressType = progressType;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    if (active.getAsBoolean()) {
      blit(graphics, getLeftOffset(), getTopOffset(), progressType.getActiveOffsetLeft(),
          progressType.getActiveOffsetTop(), progressType.getActiveWidth(), progressType.getActiveHeight());
    } else {
      blit(graphics, getLeftOffset(), getTopOffset(), progressType.getOffsetLeft(), progressType.getOffsetTop(),
          progressType.getWidth(), progressType.getHeight());
    }

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.PROCESS;
  }
}
