package com.faktocraft.common.block.impl.machines.scanner.screen;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.scanner.BlockEntityScanner;
import com.faktocraft.common.enums.ScannerMode;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class GuiScannerMode extends GuiElement {

  private static final int INVALID_ITEM_COLOR = 0xd60000;

  private final BlockEntityScanner entity;
  private boolean invalidItem = false;

  public GuiScannerMode(IGuiWrapper wrapper, BlockEntityScanner blockEntityScanner) {
    super(wrapper, 115, 6, 10, 61);
    this.entity = blockEntityScanner;
  }

  public void showInvalidItem() {
    invalidItem = true;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    if (invalidItem
        && !entity.getItemStackHandler().getStackInSlot(BlockEntityScanner.INPUT_SLOT).isEmpty()) {
      invalidItem = false;
    }

    String string;
    int color;
    if (invalidItem) {
      string = Component.translatable("gui." + Faktocraft.MODID + ".scanner.invalid_item").getString();
      color = INVALID_ITEM_COLOR;
    } else if (entity.getMode() == ScannerMode.PROGRESS) {
      string = Component.translatable(entity.getMode().getLangKey(), entity.progress.getPercentProgressString())
          .getString() + "%";
      color = entity.getMode().getColor();
    } else {
      string = Component.translatable(entity.getMode().getLangKey()).getString();
      color = entity.getMode().getColor();
    }

    float scale = Math.min(0.8f, (float) (getWidth() - 2) / minecraft.font.width(string));
    GuiUtil.renderScaled(graphics, string, getLeftOffset(), getTopOffset(), scale, color, false);
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
