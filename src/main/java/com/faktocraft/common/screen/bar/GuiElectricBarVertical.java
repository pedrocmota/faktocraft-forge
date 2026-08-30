package com.faktocraft.common.screen.bar;

import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.IndReb;
import com.faktocraft.common.energy.impl.BasicEnergyStorage;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.progress.GuiProgress;
import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiElectricBarVertical extends GuiProgress {

  private final IndRebBlockEntity blockEntity;

  public GuiElectricBarVertical(IGuiWrapper wrapper, int leftOffset, int topOffset, BasicEnergyStorage progress,
      IndRebBlockEntity blockEntity) {
    super(wrapper, leftOffset, topOffset, progress, GuiSprite.ELECTRIC_VERTICAL, Direction.VERTICAL, true);
    this.blockEntity = blockEntity;
  }

  private boolean hidden() {
    return blockEntity.hasBatteryDock() && blockEntity.getBatteryDockCapacity() <= 0;
  }

  @Override
  protected void renderBg(net.minecraft.client.gui.GuiGraphics graphics, net.minecraft.client.Minecraft minecraft,
      int mouseX, int mouseY) {
    if (hidden()) {
      return;
    }
    super.renderBg(graphics, minecraft, mouseX, mouseY);
    if (blockEntity != null && blockEntity.isDischargeMode()) {
      int alpha = (System.currentTimeMillis() / 500) % 2 == 0 ? 0x66 : 0x33;
      graphics.fill(getLeftOffset(), getTopOffset(), getLeftOffset() + getWidth(),
          getTopOffset() + getHeight(), (alpha << 24) | 0xFF8A00);
      int cx = getLeftOffset() + getWidth() / 2;
      int base = getTopOffset() + getHeight() - 7;
      graphics.fill(cx - 2, base, cx + 3, base + 2, 0xFFFFD75E);
      graphics.fill(cx - 1, base + 2, cx + 2, base + 3, 0xFFFFD75E);
      graphics.fill(cx, base + 3, cx + 1, base + 4, 0xFFFFD75E);
    } else if (blockEntity != null && blockEntity.isUndervoltage()) {
      int alpha = (System.currentTimeMillis() / 500) % 2 == 0 ? 0x66 : 0x33;
      graphics.fill(getLeftOffset(), getTopOffset(), getLeftOffset() + getWidth(),
          getTopOffset() + getHeight(), (alpha << 24) | 0xE7B416);
      int cx = getLeftOffset() + getWidth() / 2;
      int top = getTopOffset() + 7;
      graphics.fill(cx, top, cx + 1, top + 5, 0xFFFFE580);
      graphics.fill(cx, top + 6, cx + 1, top + 7, 0xFFFFE580);
    }
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.COMMON;
  }

  @Override
  protected boolean isValidClickButton(int button) {
    return button == 1 && blockEntity != null && blockEntity.hasBatteryDock();
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    com.faktocraft.common.network.ModNetworking.sendToServer(
        new com.faktocraft.common.network.packet.PacketToggleDischarge(blockEntity.getBlockPos()));
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (hidden()) {
      return;
    }
    if (isMouseOver(mouseX, mouseY)) {
      java.util.List<Component> lines = new java.util.ArrayList<>();
      lines.add(Component.translatable("gui." + IndReb.MODID + ".energy",
          TextComponentUtil.getFormattedEnergyUnit(getProgress().getProgress()),
          TextComponentUtil.getFormattedEnergyUnit(getProgress().getProgressMax())));
      if (blockEntity != null && blockEntity.isUndervoltage() && !blockEntity.isDischargeMode()) {
        lines.add(Component.translatable("gui." + IndReb.MODID + ".undervoltage")
            .withStyle(net.minecraft.ChatFormatting.YELLOW));
      }
      graphics.renderComponentTooltip(GuiUtil.getFont(), lines, mouseX, mouseY);
    }
  }
}
