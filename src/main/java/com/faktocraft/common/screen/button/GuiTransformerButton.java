package com.faktocraft.common.screen.button;

import net.minecraft.client.Minecraft;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import static com.faktocraft.Faktocraft.MODID;

public class GuiTransformerButton extends GuiButton {

  public GuiTransformerButton(IGuiWrapper wrapper, int leftOffset, int topOffset, Runnable leftClick) {
    super(wrapper, leftOffset, topOffset, GuiSprite.LARGE_BUTTON, leftClick, null);
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.renderTooltip(GuiUtil.getFont(), Component.translatable("gui." + MODID + ".change_mode"),
          mouseX, mouseY);
    }

    super.renderWidgetToolTip(screen, graphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    GuiSprite sprite = GuiSprite.TRANSFORMER_ICON;
    blit(graphics, getLeftOffset() + sprite.getRenderOffsetLeft(), getTopOffset() + sprite.getRenderOffsetTop(),
        sprite.getOffsetLeft(), sprite.getOffsetTop(), sprite.getWidth(), sprite.getHeight());
  }
}
