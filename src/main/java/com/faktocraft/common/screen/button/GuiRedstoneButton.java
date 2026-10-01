package com.faktocraft.common.screen.button;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

public class GuiRedstoneButton extends GuiButton {

  private static final ItemStack ICON_ON = new ItemStack(Items.REDSTONE_TORCH);
  private static final ItemStack ICON_OFF = new ItemStack(Items.REDSTONE);

  private final FaktocraftMenu menu;

  public GuiRedstoneButton(IGuiWrapper wrapper, FaktocraftMenu menu, int leftOffset, int topOffset) {
    super(wrapper, leftOffset, topOffset,
        com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT ? GuiSprite.TOP_BUTTON : GuiSprite.LEFT_BUTTON,
        () -> {
          Minecraft minecraft = Minecraft.getInstance();
          if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, FaktocraftMenu.BUTTON_REDSTONE_TOGGLE);
          }
        }, null);
    this.menu = menu;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    super.renderBg(graphics, minecraft, mouseX, mouseY);

    boolean corner = com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT;
    int iconLeft = getLeftOffset() + (corner ? 3 : 4);
    int iconTop = getTopOffset() + (corner ? 2 : 3);
    boolean on = menu.isRedstoneOnly();
    graphics.fakeItem(on ? ICON_ON : ICON_OFF, iconLeft, iconTop);
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      boolean on = menu.isRedstoneOnly();
      graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, List.of(
          Component.translatable("gui." + Faktocraft.MODID + ".redstone_control"),
          Component.translatable("gui." + Faktocraft.MODID
              + (on ? ".redstone_control.redstone" : ".redstone_control.always"))
              .withStyle(on ? ChatFormatting.RED : ChatFormatting.GRAY)),
          mouseX, mouseY);
    }
  }
}
