package com.faktocraft.common.screen.widgets;

import net.minecraft.client.renderer.RenderPipelines;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public abstract class GuiElement extends AbstractWidget {
  protected static final int TEXTURE_SIZE = 256;

  private final IGuiWrapper wrapper;

  public GuiElement(IGuiWrapper wrapper, int width, int height, int leftOffset, int topOffset) {
    super(wrapper.getGuiLeft() + leftOffset, wrapper.getGuiTop() + topOffset, width, height, Component.empty());
    this.wrapper = wrapper;
  }

  public IGuiWrapper getWrapper() {
    return wrapper;
  }

  public int getLeftOffset() {
    return getX();
  }

  public int getTopOffset() {
    return getY();
  }

  @Override
  protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    renderBg(graphics, Minecraft.getInstance(), mouseX, mouseY);
  }

  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
  }

  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
  }

  public Identifier getResourceLocation() {
    return Constants.COMMON;
  }

  protected void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v, int width,
      int height) {
    if (width < 0) {
      x += width;
      u += width;
      width = -width;
    }
    if (height < 0) {
      y += height;
      v += height;
      height = -height;
    }
    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, TEXTURE_SIZE,
        TEXTURE_SIZE);
  }

  protected void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
    blit(graphics, getResourceLocation(), x, y, u, v, width, height);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {
  }
}
