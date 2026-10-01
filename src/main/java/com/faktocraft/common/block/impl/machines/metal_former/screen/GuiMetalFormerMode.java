package com.faktocraft.common.block.impl.machines.metal_former.screen;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.metal_former.BlockEntityMetalFormer;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.button.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiMetalFormerMode extends GuiButton {

  public GuiMetalFormerMode(IGuiWrapper wrapper, int leftOffset, int topOffset, BlockEntityMetalFormer entity,
      Runnable leftClick) {
    super(wrapper, leftOffset, topOffset, GuiSprite.LARGE_BUTTON, leftClick, null);
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.setTooltipForNextFrame(com.faktocraft.common.util.GuiUtil.getFont(),
          Component.translatable("gui." + Faktocraft.MODID + ".change_mode"), mouseX, mouseY);
    }
    super.renderWidgetToolTip(screen, graphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    GuiSprite sprite = GuiSprite.TRANSFORMER_ICON;
    blit(graphics, getLeftOffset() + sprite.getRenderOffsetLeft(), getTopOffset() + sprite.getRenderOffsetTop(),
        sprite.getOffsetLeft(), sprite.getOffsetTop(), sprite.getWidth(), sprite.getHeight());
  }
}
