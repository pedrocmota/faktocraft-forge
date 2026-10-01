package com.faktocraft.common.screen.button;

import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

public class GuiPriorityButton extends GuiButton {

  private final FaktocraftMenu menu;

  public GuiPriorityButton(IGuiWrapper wrapper, FaktocraftMenu menu, int leftOffset, int topOffset) {
    super(wrapper, leftOffset, topOffset,
        com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT ? GuiSprite.TOP_BUTTON : GuiSprite.LEFT_BUTTON,
        () -> {
          Minecraft minecraft = Minecraft.getInstance();
          if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, FaktocraftMenu.BUTTON_PRIORITY_CYCLE);
          }
        }, null);
    this.menu = menu;
  }

  private int mode() {
    return menu.getGeneratorPriorityMode();
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    boolean corner = com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT;
    int mode = mode();
    String label = mode == 0 ? "A" : String.valueOf(mode);
    int textLeft = getLeftOffset() + (corner ? 10 : 11) - minecraft.font.width(label) / 2;
    int textTop = getTopOffset() + (corner ? 7 : 8);
    graphics.text(minecraft.font, label, textLeft, textTop, GuiUtil.opaque(mode == 0 ? 0xB0E0B0 : 0xFFD966));
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      int mode = mode();
      Component value = mode == 0
          ? Component.translatable("gui." + Faktocraft.MODID + ".generator_priority.auto",
              menu.getGeneratorPriorityDefault()).withStyle(ChatFormatting.GREEN)
          : Component.translatable("gui." + Faktocraft.MODID + ".generator_priority.manual", mode)
              .withStyle(ChatFormatting.GOLD);
      graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, List.of(
          Component.translatable("gui." + Faktocraft.MODID + ".generator_priority"),
          value), mouseX, mouseY);
    }
  }
}
