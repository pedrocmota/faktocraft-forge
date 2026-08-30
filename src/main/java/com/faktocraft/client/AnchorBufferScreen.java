package com.faktocraft.client;

import com.faktocraft.IndReb;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketAnchorBuffer;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class AnchorBufferScreen extends Screen {

  private static final int PANEL_W = 200;
  private static final int PANEL_H = 108;

  private static final int STEP = 1_000;

  private final BlockPos target;
  private final int current;
  private final int min;
  private final int max;
  private int selected;

  private AnchorBufferScreen(BlockPos target, int current, int min, int max) {
    super(Component.translatable("gui." + IndReb.MODID + ".anchor_buffer"));
    this.target = target;
    this.current = current;
    this.min = min;
    this.max = max;
    this.selected = current;
  }

  public static void open(BlockPos target, int current, int min, int max) {
    Minecraft.getInstance().setScreen(new AnchorBufferScreen(target, current, min, max));
  }

  @Override
  protected void init() {
    int left = (width - PANEL_W) / 2;
    int top = (height - PANEL_H) / 2;
    double initial = max > min ? (double) (current - min) / (max - min) : 1.0;
    addRenderableWidget(new AbstractSliderButton(left + 14, top + 32, PANEL_W - 28, 20,
        sliderLabel(current), Mth.clamp(initial, 0.0, 1.0)) {
      @Override
      protected void updateMessage() {
        setMessage(sliderLabel(selected));
      }

      @Override
      protected void applyValue() {
        int raw = min + (int) Math.round(value * (max - min));
        selected = Mth.clamp(Math.round((float) raw / STEP) * STEP, min, max);
        if (max > min) {
          value = (double) (selected - min) / (max - min);
        }
      }
    });
    addRenderableWidget(Button.builder(
        Component.translatable("gui." + IndReb.MODID + ".anchor_buffer.apply"),
        button -> apply()).bounds(left + 14, top + 74, PANEL_W - 28, 20).build());
  }

  private static Component sliderLabel(int capacity) {
    return Component.literal(TextComponentUtil.getFormattedEnergyUnit(capacity) + " IE");
  }

  private void apply() {
    if (selected != current) {
      ModNetworking.sendToServer(new PacketAnchorBuffer(target, Mth.clamp(selected, min, max)));
    }
    onClose();
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
      apply();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);
    int left = (width - PANEL_W) / 2;
    int top = (height - PANEL_H) / 2;
    graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xE8101014);
    graphics.fill(left, top, left + PANEL_W, top + 1, 0xFF8A8A96);
    graphics.fill(left, top + PANEL_H - 1, left + PANEL_W, top + PANEL_H, 0xFF8A8A96);
    graphics.fill(left, top, left + 1, top + PANEL_H, 0xFF8A8A96);
    graphics.fill(left + PANEL_W - 1, top, left + PANEL_W, top + PANEL_H, 0xFF8A8A96);
    graphics.drawCenteredString(font, title.copy().withStyle(ChatFormatting.BOLD),
        left + PANEL_W / 2, top + 14, 0xFFE8C43A);
    graphics.drawCenteredString(font,
        Component.translatable("gui." + IndReb.MODID + ".anchor_buffer.hint",
            TextComponentUtil.getFormattedEnergyUnit(min),
            TextComponentUtil.getFormattedEnergyUnit(max)).withStyle(ChatFormatting.GRAY),
        left + PANEL_W / 2, top + 58, 0xA0A0A0);
    super.render(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
