package com.faktocraft.common.block.impl.machines.canning_machine;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.button.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiCanningMode extends GuiButton {

  private final BlockEntityCanningMachine entity;

  public GuiCanningMode(IGuiWrapper wrapper, int leftOffset, int topOffset, BlockEntityCanningMachine entity,
      Runnable leftClick) {
    super(wrapper, leftOffset, topOffset, GuiSprite.LARGE_BUTTON, leftClick, null);
    this.entity = entity;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.renderTooltip(com.faktocraft.common.util.GuiUtil.getFont(),
          Component.translatable("gui." + Faktocraft.MODID + ".canning_mode." + entity.getMode().getType()),
          mouseX, mouseY);
    }
    super.renderWidgetToolTip(screen, graphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    GuiSprite sprite = entity.getMode().getSprite();
    blit(graphics, getLeftOffset() + 4, getTopOffset() + 4,
        sprite.getOffsetLeft(), sprite.getOffsetTop(), sprite.getWidth(), sprite.getHeight());
  }
}
