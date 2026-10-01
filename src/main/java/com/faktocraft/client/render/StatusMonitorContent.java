package com.faktocraft.client.render;

import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.BlockStatusMonitor;
import com.faktocraft.common.block.impl.monitor.StatusLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import java.util.List;

public final class StatusMonitorContent {

  public static final int PANEL_W = 64 * BlockStatusMonitor.WIDTH;
  public static final int PANEL_H = 64 * BlockStatusMonitor.HEIGHT;
  public static final int TRACK_COLOR = 0xFF3A3D44;
  private static final int PADDING = 6;
  public static final int CONTENT_W = PANEL_W - 2 * PADDING;
  public static final int CONTENT_H = PANEL_H - 2 * PADDING;
  private static final int LINE_H = 10;
  private static final int BAR_H = 9;
  private static final int ICON_SIZE = 16;
  private static final int FRAME_COLOR = 0xFF8A8A8A;
  private static final int TEXT_COLOR = 0xFFE8E8E8;
  private static final int DIM_COLOR = 0xFF9A9A9A;
  private static final int MOD_COLOR = 0xFF5F8DE8;
  private static final int UNLOADED_COLOR = 0xFFE0A030;
  private static final int MISSING_COLOR = 0xFFE05050;

  private StatusMonitorContent() {
  }

  public static void draw(GuiGraphicsExtractor graphics, BlockEntityStatusMonitor monitor) {
    drawFrame(graphics, monitor.joinedLeft(), monitor.joinedRight());
    graphics.pose().pushMatrix();
    graphics.pose().translate(PADDING, PADDING);
    drawContent(graphics, monitor);
    graphics.pose().popMatrix();
  }

  private static void drawFrame(GuiGraphicsExtractor graphics, boolean joinedLeft, boolean joinedRight) {
    graphics.fill(0, 0, PANEL_W, 1, FRAME_COLOR);
    graphics.fill(0, PANEL_H - 1, PANEL_W, PANEL_H, FRAME_COLOR);
    if (!joinedLeft) {
      graphics.fill(0, 0, 1, PANEL_H, FRAME_COLOR);
    }
    if (!joinedRight) {
      graphics.fill(PANEL_W - 1, 0, PANEL_W, PANEL_H, FRAME_COLOR);
    }
  }

  private static void drawContent(GuiGraphicsExtractor graphics, BlockEntityStatusMonitor monitor) {
    Font font = Minecraft.getInstance().font;
    int width = CONTENT_W;
    int height = CONTENT_H;
    int status = monitor.status();
    if (status == BlockEntityStatusMonitor.STATUS_NONE) {
      text(graphics, font, Component.translatable("gui.faktocraft.status_monitor.no_target"), 0, 0, DIM_COLOR,
          width);
      return;
    }
    Object bridge = monitor.bridge();
    if (bridge != null && status == BlockEntityStatusMonitor.STATUS_OK) {
      graphics.pose().pushMatrix();
      boolean drawn = StatusClientBridges.render(bridge, graphics, width, height);
      graphics.pose().popMatrix();
      if (drawn) {
        return;
      }
    }
    BlockState target = monitor.targetState();
    int y = 0;
    if (!target.isAir()) {
      ItemStack icon = new ItemStack(target.getBlock());
      if (!icon.isEmpty()) {
        graphics.item(icon, 0, 0);
      }
      int textX = icon.isEmpty() ? 0 : ICON_SIZE + 4;
      text(graphics, font, target.getBlock().getName().copy().withStyle(ChatFormatting.WHITE), textX, 0,
          TEXT_COLOR, width - textX);
      text(graphics, font, Component.literal(modName(target)).withStyle(ChatFormatting.ITALIC), textX, LINE_H,
          MOD_COLOR, width - textX);
      y = ICON_SIZE + 4;
    }
    if (status == BlockEntityStatusMonitor.STATUS_UNLOADED) {
      text(graphics, font, Component.translatable("gui.faktocraft.status_monitor.unloaded"), 0, y, UNLOADED_COLOR,
          width);
      y += LINE_H;
    } else if (status == BlockEntityStatusMonitor.STATUS_MISSING) {
      text(graphics, font, Component.translatable("gui.faktocraft.status_monitor.missing"), 0, y, MISSING_COLOR,
          width);
      y += LINE_H;
    }
    List<StatusLine> lines = monitor.lines();
    for (StatusLine line : lines) {
      int needed = line instanceof StatusLine.Bar ? BAR_H + 2 : line instanceof StatusLine.Item
          ? ICON_SIZE : LINE_H;
      if (y + needed > height) {
        break;
      }
      if (line instanceof StatusLine.Text textLine) {
        text(graphics, font, textLine.text(), 0, y, TEXT_COLOR, width);
        y += LINE_H;
      } else if (line instanceof StatusLine.Bar bar) {
        drawBar(graphics, font, bar, y, width);
        y += BAR_H + 2;
      } else if (line instanceof StatusLine.Item item) {
        graphics.item(item.stack(), 0, y);
        text(graphics, font, item.label(), ICON_SIZE + 4, y + 4, TEXT_COLOR, width - ICON_SIZE - 4);
        y += ICON_SIZE;
      }
    }
  }

  private static String modName(BlockState state) {
    String namespace = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();
    return ModList.get().getModContainerById(namespace)
        .map(container -> container.getModInfo().getDisplayName()).orElse(namespace);
  }

  private static void drawBar(GuiGraphicsExtractor graphics, Font font, StatusLine.Bar bar, int y, int width) {
    graphics.fill(0, y, width, y + BAR_H, TRACK_COLOR);
    int filled = Math.round(width * Math.max(0.0F, Math.min(1.0F, bar.ratio())));
    if (filled > 0) {
      graphics.fill(0, y, filled, y + BAR_H, bar.color());
    }
    FormattedCharSequence label = bar.label().getVisualOrderText();
    int textWidth = font.width(label);
    int x = Math.max(0, (width - textWidth) / 2);
    graphics.text(font, label, x, y + 1, 0xFFFFFFFF, true);
  }

  private static void text(GuiGraphicsExtractor graphics, Font font, Component component, int x, int y, int color,
      int maxWidth) {
    FormattedCharSequence sequence = font.split(component, maxWidth).stream().findFirst()
        .orElse(component.getVisualOrderText());
    graphics.text(font, sequence, x, y, GuiUtil.opaque(color), false);
  }
}
