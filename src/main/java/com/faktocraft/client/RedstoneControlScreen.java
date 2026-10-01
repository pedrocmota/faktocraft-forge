package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketRedstoneControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class RedstoneControlScreen extends Screen {

  private static final int PANEL_W = 200;
  private static final int PANEL_H = 74;

  private final BlockPos target;
  private boolean redstoneOnly;

  public RedstoneControlScreen(BlockPos target, boolean redstoneOnly) {
    super(Component.translatable("gui." + Faktocraft.MODID + ".redstone_control"));
    this.target = target;
    this.redstoneOnly = redstoneOnly;
  }

  public static void open(BlockPos target, boolean current) {
    Minecraft.getInstance().gui.setScreen(new RedstoneControlScreen(target, current));
  }

  private Component toggleLabel() {
    return Component.translatable("gui." + Faktocraft.MODID
        + (redstoneOnly ? ".redstone_control.redstone_only" : ".redstone_control.manual"));
  }

  @Override
  protected void init() {
    int left = (width - PANEL_W) / 2;
    int top = (height - PANEL_H) / 2;
    addRenderableWidget(Button.builder(toggleLabel(), button -> {
      redstoneOnly = !redstoneOnly;
      ModNetworking.sendToServer(new PacketRedstoneControl(target, redstoneOnly));
      button.setMessage(toggleLabel());
    }).bounds(left + 14, top + 34, PANEL_W - 28, 20).build());
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    int left = (width - PANEL_W) / 2;
    int top = (height - PANEL_H) / 2;
    graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xE8101014);
    graphics.fill(left, top, left + PANEL_W, top + 1, 0xFF8A8A96);
    graphics.fill(left, top + PANEL_H - 1, left + PANEL_W, top + PANEL_H, 0xFF8A8A96);
    graphics.fill(left, top, left + 1, top + PANEL_H, 0xFF8A8A96);
    graphics.fill(left + PANEL_W - 1, top, left + PANEL_W, top + PANEL_H, 0xFF8A8A96);
    graphics.centeredText(font, title.copy().withStyle(ChatFormatting.BOLD),
        left + PANEL_W / 2, top + 14, 0xFFE8C43A);
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean isInGameUi() {
    return true;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
