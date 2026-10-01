package com.faktocraft.common.screen.bar;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor;
import com.faktocraft.common.interfaces.block.IGenerationInfo;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

public class GuiHeatBar extends GuiElement {

  private static final int WIDTH = 16;
  private static final int HEIGHT = 49;
  private static final int INNER = HEIGHT - 2;

  private final BlockEntityNuclearReactor reactor;

  public GuiHeatBar(IGuiWrapper wrapper, int leftOffset, int topOffset, BlockEntityNuclearReactor reactor) {
    super(wrapper, WIDTH, HEIGHT, leftOffset, topOffset);
    this.reactor = reactor;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    int x = getX();
    int y = getY();
    graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF1E1E1E);
    graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, 0xFF3A3A3A);
    float ratio = reactor.heatRatio();
    int filled = Math.round(ratio * INNER);
    if (filled > 0) {
      graphics.fill(x + 1, y + HEIGHT - 1 - filled, x + WIDTH - 1, y + HEIGHT - 1, heatColor(ratio));
    }
    int hot = y + HEIGHT - 1 - Math.round(BlockEntityNuclearReactor.HOT_RATIO * INNER);
    int critical = y + HEIGHT - 1 - Math.round(BlockEntityNuclearReactor.CRITICAL_RATIO * INNER);
    graphics.fill(x + 1, hot, x + WIDTH - 1, hot + 1, 0x90FFFFFF);
    graphics.fill(x + 1, critical, x + WIDTH - 1, critical + 1, 0xC0FF5050);
  }

  public static int heatColor(float ratio) {
    int from;
    int to;
    float t;
    if (ratio < BlockEntityNuclearReactor.HOT_RATIO) {
      from = 0x3CC33C;
      to = 0xE8C43A;
      t = ratio / BlockEntityNuclearReactor.HOT_RATIO;
    } else {
      from = 0xE8C43A;
      to = 0xE03030;
      t = (ratio - BlockEntityNuclearReactor.HOT_RATIO) / (1.0F - BlockEntityNuclearReactor.HOT_RATIO);
    }
    t = Math.max(0.0F, Math.min(1.0F, t));
    int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
    int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
    int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }

  public static ChatFormatting statusColor(int status) {
    return switch (status) {
      case BlockEntityNuclearReactor.STATUS_RUNNING -> ChatFormatting.GREEN;
      case BlockEntityNuclearReactor.STATUS_FULL -> ChatFormatting.YELLOW;
      case BlockEntityNuclearReactor.STATUS_REDSTONE, BlockEntityNuclearReactor.STATUS_HOT -> ChatFormatting.GOLD;
      case BlockEntityNuclearReactor.STATUS_CRITICAL -> ChatFormatting.RED;
      default -> ChatFormatting.GRAY;
    };
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (!isMouseOver(mouseX, mouseY)) {
      return;
    }
    String prefix = "gui." + Faktocraft.MODID + ".reactor.";
    int status = reactor.getStatus();
    List<Component> lines = List.of(
        Component.translatable(prefix + "heat", Math.round(reactor.heatRatio() * 100.0F))
            .withStyle(ChatFormatting.WHITE),
        Component.translatable(BlockEntityNuclearReactor.statusKey(status)).withStyle(statusColor(status)),
        Component.translatable(prefix + "output", IGenerationInfo.rate(reactor.getOutputPerTick()))
            .withStyle(ChatFormatting.GRAY),
        Component.translatable(prefix + "rods", reactor.getRods()).withStyle(ChatFormatting.GRAY),
        Component.translatable(prefix + "heating", reactor.getHeatingPerTick())
            .withStyle(ChatFormatting.RED),
        Component.translatable(prefix + "cooling", reactor.getCoolingPerTick()).withStyle(ChatFormatting.AQUA));
    graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lines, mouseX, mouseY);
  }
}
