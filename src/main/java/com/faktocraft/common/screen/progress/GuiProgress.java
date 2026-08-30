package com.faktocraft.common.screen.progress;

import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiProgress extends GuiElement {

  private final IProgress progress;
  private GuiSprite progressType;
  private final Direction direction;
  private final boolean reverse;

  public GuiProgress(IGuiWrapper wrapper, int leftOffset, int topOffset, IProgress progress, GuiSprite progressType,
      Direction direction, boolean reverse) {
    super(wrapper, progressType.getWidth(), progressType.getHeight(), leftOffset, topOffset);
    this.progress = progress;
    this.progressType = progressType;
    this.direction = direction;
    this.reverse = reverse;
  }

  public void setProgressType(GuiSprite progressType) {
    this.progressType = progressType;
  }

  public IProgress getProgress() {
    return progress;
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.PROCESS;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    blit(graphics, getLeftOffset(), getTopOffset(), progressType.getOffsetLeft(), progressType.getOffsetTop(),
        progressType.getWidth(), progressType.getHeight());

    float currProgress = progress.getPercentProgress();
    int scaleX = Math.round(currProgress / 100 * progressType.getActiveHeight());
    int scaleY = Math.round(currProgress / 100 * progressType.getActiveWidth());

    switch (direction) {
      case VERTICAL:
        if (reverse) {
          blit(
              graphics,
              getLeftOffset() + progressType.getRenderOffsetLeft(),
              getTopOffset() + progressType.getRenderOffsetTop() + progressType.getActiveHeight() - scaleX,
              progressType.getActiveOffsetLeft(),
              progressType.getActiveOffsetTop() + progressType.getActiveHeight() - scaleX,
              progressType.getActiveWidth(),
              scaleX);
        } else {
          blit(
              graphics,
              getLeftOffset() + progressType.getRenderOffsetLeft(),
              getTopOffset() + progressType.getRenderOffsetTop() + scaleX,
              progressType.getActiveOffsetLeft(),
              progressType.getActiveOffsetTop() + scaleX,
              progressType.getActiveWidth(),
              scaleX * -1);
        }
        break;
      case HORIZONTAL:
        if (reverse) {
          blit(
              graphics,
              getLeftOffset() + progressType.getRenderOffsetLeft(),
              getTopOffset() + progressType.getRenderOffsetTop(),
              progressType.getActiveOffsetLeft(),
              progressType.getActiveOffsetTop(),
              progressType.getActiveWidth() - scaleY,
              progressType.getActiveHeight());
        } else {
          blit(
              graphics,
              getLeftOffset() + progressType.getRenderOffsetLeft(),
              getTopOffset() + progressType.getRenderOffsetTop(),
              progressType.getActiveOffsetLeft(),
              progressType.getActiveOffsetTop(),
              scaleY,
              progressType.getActiveHeight());
        }
        break;
    }

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  public enum Direction {
    VERTICAL,
    HORIZONTAL
  }
}
