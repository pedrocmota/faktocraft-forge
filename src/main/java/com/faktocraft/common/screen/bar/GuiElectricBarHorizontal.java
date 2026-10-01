package com.faktocraft.common.screen.bar;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.input.MouseButtonEvent;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.entity.IProgress;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.progress.GuiProgress;
import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiElectricBarHorizontal extends GuiProgress {

  @org.jetbrains.annotations.Nullable
  private final com.faktocraft.common.entity.block.FaktocraftBlockEntity blockEntity;

  public GuiElectricBarHorizontal(IGuiWrapper wrapper, int leftOffset, int topOffset, IProgress progress) {
    this(wrapper, leftOffset, topOffset, progress, null);
  }

  public GuiElectricBarHorizontal(IGuiWrapper wrapper, int leftOffset, int topOffset, IProgress progress,
      @org.jetbrains.annotations.Nullable com.faktocraft.common.entity.block.FaktocraftBlockEntity blockEntity) {
    super(wrapper, leftOffset, topOffset, progress, GuiSprite.ELECTRIC_HORIZONTAL, Direction.HORIZONTAL, false);
    this.blockEntity = blockEntity;
  }

  private boolean hidden() {
    return blockEntity != null && blockEntity.hasBatteryDock() && blockEntity.getBatteryDockCapacity() <= 0;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, net.minecraft.client.Minecraft minecraft, int mouseX,
      int mouseY) {
    if (hidden()) {
      return;
    }
    super.renderBg(graphics, minecraft, mouseX, mouseY);
    if (blockEntity != null && blockEntity.isDischargeMode()) {
      int alpha = (System.currentTimeMillis() / 500) % 2 == 0 ? 0x66 : 0x33;
      graphics.fill(getLeftOffset(), getTopOffset(), getLeftOffset() + getWidth(),
          getTopOffset() + getHeight(), (alpha << 24) | 0xFF8A00);
      int cy = getTopOffset() + getHeight() / 2;
      int base = getLeftOffset() + getWidth() - 7;
      graphics.fill(base, cy - 2, base + 2, cy + 3, 0xFFFFD75E);
      graphics.fill(base + 2, cy - 1, base + 3, cy + 2, 0xFFFFD75E);
      graphics.fill(base + 3, cy, base + 4, cy + 1, 0xFFFFD75E);
    } else if (blockEntity != null && blockEntity.isUndervoltage()) {
      int alpha = (System.currentTimeMillis() / 500) % 2 == 0 ? 0x66 : 0x33;
      graphics.fill(getLeftOffset(), getTopOffset(), getLeftOffset() + getWidth(),
          getTopOffset() + getHeight(), (alpha << 24) | 0xE7B416);
      int cy = getTopOffset() + getHeight() / 2;
      int left = getLeftOffset() + 4;
      graphics.fill(left, cy - 3, left + 1, cy + 2, 0xFFFFE580);
      graphics.fill(left, cy + 3, left + 1, cy + 4, 0xFFFFE580);
    }
  }

  @Override
  public Identifier getResourceLocation() {
    return Constants.COMMON;
  }

  @Override
  protected boolean isValidClickButton(MouseButtonInfo button) {
    return button.button() == InputConstants.MOUSE_BUTTON_RIGHT && blockEntity != null
        && blockEntity.hasBatteryDock();
  }

  @Override
  public void onClick(MouseButtonEvent event, boolean doubleClick) {
    com.faktocraft.common.network.ModNetworking.sendToServer(
        new com.faktocraft.common.network.packet.PacketToggleDischarge(blockEntity.getBlockPos()));
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (hidden()) {
      return;
    }
    if (isMouseOver(mouseX, mouseY)) {
      java.util.List<Component> lines = new java.util.ArrayList<>();
      lines.add(Component.translatable("gui." + Faktocraft.MODID + ".energy",
          TextComponentUtil.getFormattedEnergyUnit(getProgress().getProgress()),
          TextComponentUtil.getFormattedEnergyUnit(getProgress().getProgressMax())));
      if (blockEntity != null && blockEntity.isUndervoltage() && !blockEntity.isDischargeMode()) {
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".undervoltage")
            .withStyle(net.minecraft.ChatFormatting.YELLOW));
      }
      graphics.setComponentTooltipForNextFrame(GuiUtil.getFont(), lines, mouseX, mouseY);
    }
  }
}
