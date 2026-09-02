package com.faktocraft.common.screen.button;

import net.minecraft.client.Minecraft;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class GuiInfoButton extends GuiButton {

  ISupportUpgrades supportUpgrades;

  public GuiInfoButton(IGuiWrapper wrapper, ISupportUpgrades supportUpgrades, int topOffset) {
    this(wrapper, supportUpgrades, -19, topOffset);
  }

  public GuiInfoButton(IGuiWrapper wrapper, ISupportUpgrades supportUpgrades, int leftOffset, int topOffset) {
    super(wrapper, leftOffset, topOffset,
        com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT ? GuiSprite.TOP_BUTTON : GuiSprite.LEFT_BUTTON,
        null, null);
    this.supportUpgrades = supportUpgrades;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      List<Component> elements = new ArrayList<>();
      elements
          .add(Component.translatable(EnumLang.SUPPORTED_UPGRADES.getTranslationKey()).withStyle(ChatFormatting.AQUA));

      for (UpgradeType ut : supportUpgrades.getSupportedUpgrades()) {
        elements.add(ut.getLang().getTranslationComponent());
      }

      if (supportUpgrades instanceof com.faktocraft.common.entity.block.FaktocraftBlockEntity be
          && be.hasBatteryDock()) {
        elements.add(Component.translatable("gui.faktocraft.discharge_hint")
            .withStyle(ChatFormatting.DARK_GRAY));
      }

      graphics.renderComponentTooltip(GuiUtil.getFont(), elements, mouseX, mouseY);
    }
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    GuiSprite sprite = GuiSprite.INFO_ICON;
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
