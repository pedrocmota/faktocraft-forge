package com.faktocraft.common.screen.widgets;

import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class GuiTextProgress extends GuiElement {

  private final IProgress progress;
  private final String prepend;
  private final String append;

  public GuiTextProgress(IGuiWrapper wrapper, int width, int height, int leftOffset, int topOffset, IProgress progress,
      String prepend, String append) {
    super(wrapper, width, height, leftOffset, topOffset);
    this.progress = progress;
    this.prepend = prepend;
    this.append = append;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    String currProgress = progress.getPercentProgressString();
    GuiUtil.renderScaled(graphics, prepend + currProgress + append, getLeftOffset(), getTopOffset(), 0.8f, 4210752,
        false);
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
