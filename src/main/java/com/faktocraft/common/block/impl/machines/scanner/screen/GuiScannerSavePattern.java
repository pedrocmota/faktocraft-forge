package com.faktocraft.common.block.impl.machines.scanner.screen;

import net.minecraftforge.registries.ForgeRegistries;
import com.faktocraft.common.block.impl.machines.scanner.BlockEntityScanner;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.button.GuiButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class GuiScannerSavePattern extends GuiButton {

  private final BlockEntityScanner entity;

  public GuiScannerSavePattern(IGuiWrapper wrapper, int leftOffset, int topOffset,
      BlockEntityScanner blockEntityScanner, Runnable leftClick) {
    super(wrapper, leftOffset, topOffset, GuiSprite.SCANNER_SAVE, leftClick, null);
    this.entity = blockEntityScanner;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    if (entity.getMode().getId() >= 4) {
      super.renderBg(graphics, minecraft, mouseX, mouseY);
    }
  }

  @Override
  protected boolean onLeftClick() {
    if (entity.getMode().getId() >= 4) {
      super.onLeftClick();
    }
    return false;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY) && entity.getMode().getId() >= 4) {
      ResourceLocation rl = ForgeRegistries.ITEMS.getKey(entity.getResult().getResultStack().getItem());
      graphics.renderComponentTooltip(com.faktocraft.common.util.GuiUtil.getFont(), List.of(
          EnumLang.SAVE_PATTERN.getTranslationComponent(),
          Component.literal(entity.getResult().getResultStack().getHoverName().getString())
              .withStyle(ChatFormatting.GRAY),
          Component.translatable(rl.toString()).withStyle(ChatFormatting.DARK_GRAY)), mouseX, mouseY);
    }
  }
}
