package com.faktocraft.common.screen.text;

import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;

public class GuiTextStatus extends GuiElement {

  private final Supplier<StatusLine> status;

  public record StatusLine(Component text, int color) {
  }

  public GuiTextStatus(IGuiWrapper wrapper, int width, int height, int leftOffset, int topOffset,
      Supplier<StatusLine> status) {
    super(wrapper, width, height, leftOffset, topOffset);
    this.status = status;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    StatusLine line = status.get();
    if (line != null) {
      Font font = GuiUtil.getFont();
      String text = line.text().getString();
      float scale = Math.min(0.8f, (float) getWidth() / font.width(text));
      float left = getLeftOffset() + getWidth() / 2.0f - font.width(text) * scale / 2.0f;
      GuiUtil.renderScaled(graphics, text, (int) left, getTopOffset(), scale, line.color(), false);
    }
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
