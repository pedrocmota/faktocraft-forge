package com.faktocraft.common.screen.text;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GuiTextHeat extends GuiElement {

  private final IProgress progress;

  public GuiTextHeat(IGuiWrapper wrapper, int width, int height, int leftOffset, int topOffset, IProgress progress) {
    super(wrapper, width, height, leftOffset, topOffset);
    this.progress = progress;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    MutableComponent component = Component.translatable("gui." + Faktocraft.MODID + ".heat",
        progress.getPercentProgressString() + "%");
    GuiUtil.renderScaled(graphics, component.getString(), getLeftOffset(), getTopOffset(), 0.8f, 4210752, false);
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
