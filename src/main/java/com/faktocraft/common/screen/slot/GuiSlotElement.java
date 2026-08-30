package com.faktocraft.common.screen.slot;

import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.interfaces.entity.ISlot;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiSlotElement extends GuiElement {

  private final ISlot slot;

  public GuiSlotElement(IGuiWrapper wrapper, ISlot slot) {
    super(wrapper, slot.guiSlotType().getWidth(), slot.guiSlotType().getHeight(), slot.getGuiX(), slot.getGuiY());
    this.slot = slot;
  }

  public GuiSlotType getSlotType() {
    return slot.guiSlotType();
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {

    boolean hideBoltPlaceholder = getSlotType() == GuiSlotType.NORMAL_BLANK
        && !getWrapper().getBlockEntity().getItemStackHandler().getStackInSlot(slot.getSlotId()).isEmpty();
    if (!hideBoltPlaceholder) {
      blit(graphics, getLeftOffset(), getTopOffset(), getSlotType().getOffsetLeft(), getSlotType().getOffsetTop(),
          getSlotType().getWidth(), getSlotType().getHeight());
    }

    if (getSlotType() == GuiSlotType.BATTERY_DOCK) {
      blit(graphics, getLeftOffset() + 1, getTopOffset() + 1, 110, 0, 16, 16);
    } else if (getSlotType() == GuiSlotType.TENSION_DOCK) {
      blit(graphics, getLeftOffset() + 1, getTopOffset() + 1, 84, 46, 16, 16);
    } else if (getSlotType() == GuiSlotType.DOCK_BATTERY) {
      blit(graphics, getLeftOffset() + 1, getTopOffset() + 1, 104, 28, 16, 16);
    }

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.PROCESS;
  }
}
