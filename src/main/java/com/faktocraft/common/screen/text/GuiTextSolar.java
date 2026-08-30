package com.faktocraft.common.screen.text;

import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.function.IntSupplier;

public class GuiTextSolar extends GuiElement {

  private final IntSupplier amount;

  public GuiTextSolar(IGuiWrapper wrapper, int width, int height, int leftOffset, int topOffset, IntSupplier amount) {
    super(wrapper, width, height, leftOffset, topOffset);
    this.amount = amount;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    MutableComponent component = Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
        TextComponentUtil.getFormattedEnergyUnit(amount.getAsInt()));
    Font font = GuiUtil.getFont();
    float left = (float) (getLeftOffset() + 5 - font.width(component) / 2);
    GuiUtil.renderScaled(graphics, component.getString(), (int) left, getTopOffset(), 0.8f, 4210752, false);
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
