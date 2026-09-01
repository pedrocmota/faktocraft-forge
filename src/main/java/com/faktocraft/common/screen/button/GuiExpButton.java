package com.faktocraft.common.screen.button;

import net.minecraft.client.Minecraft;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;
import static com.faktocraft.Faktocraft.MODID;

public class GuiExpButton extends GuiButton {

  IExpCollector expCollector;

  public GuiExpButton(IGuiWrapper wrapper, IExpCollector expCollector, int topOffset) {
    this(wrapper, expCollector, -19, topOffset);
  }

  public GuiExpButton(IGuiWrapper wrapper, IExpCollector expCollector, int leftOffset, int topOffset) {
    super(wrapper, leftOffset, topOffset,
        com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT ? GuiSprite.TOP_BUTTON : GuiSprite.LEFT_BUTTON,
        expCollector.collectExp(), null);
    this.expCollector = expCollector;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      graphics.renderComponentTooltip(GuiUtil.getFont(),
          List.of(
              Component.translatable("gui." + MODID + ".collect_exp").withStyle(ChatFormatting.GREEN),
              Component.literal(expCollector.getStoredExperience() + " EXP")),
          mouseX, mouseY);
    }
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    GuiSprite sprite = GuiSprite.EXP_ICON;
    boolean corner = com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT;
    int iconLeft = corner
        ? getLeftOffset() + 4
        : getLeftOffset() + sprite.getRenderOffsetLeft();
    int iconTop = corner
        ? getTopOffset() + 3
        : getTopOffset() + sprite.getRenderOffsetTop();
    blit(graphics, iconLeft, iconTop,
        sprite.getOffsetLeft(), sprite.getOffsetTop(), sprite.getWidth(), sprite.getHeight());
  }
}
