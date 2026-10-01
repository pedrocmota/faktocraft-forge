package com.faktocraft.client;

import net.minecraft.client.renderer.RenderPipelines;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.List;

public class InfoPanelScreen extends Screen {

  private static final Identifier PANEL = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/pipe_motor.png");

  protected interface Row {
  }

  protected record TextRow(FormattedText text, int color) implements Row {
    protected TextRow(String plain, int color) {
      this(FormattedText.of(plain), color);
    }
  }

  protected record StatusRow(String text, int color) implements Row {
  }

  protected record SeparatorRow() implements Row {
  }

  protected record BarRow(String label, float ratio, int fillColor, String value) implements Row {
  }

  protected final List<String> lines = new ArrayList<>();
  protected final List<Row> rows = new ArrayList<>();

  public InfoPanelScreen(Component title) {
    super(title);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    List<Row> source = new ArrayList<>();
    if (!rows.isEmpty()) {
      source.addAll(rows);
    } else {
      for (String line : lines) {
        source.add(new TextRow(line, 0x404040));
      }
    }

    int maxWidth = (int) (156 / 0.9f);
    record DrawOp(Row row, FormattedCharSequence wrapped) {
    }
    List<DrawOp> ops = new ArrayList<>();
    int contentHeight = 0;
    for (Row row : source) {
      if (row instanceof TextRow text) {
        for (FormattedCharSequence seq : this.font.split(text.text(), maxWidth)) {
          ops.add(new DrawOp(new TextRow("", text.color()), seq));
          contentHeight += 12;
        }
      } else {
        ops.add(new DrawOp(row, null));
        contentHeight += row instanceof SeparatorRow ? 7 : 13;
      }
    }

    int totalHeight = 34 + contentHeight + 10;
    int middles = Math.max(0, (totalHeight - 45 + 39) / 40);
    int panelHeight = 40 + middles * 40 + 5;
    int left = this.width / 2 - 88;
    int top = this.height / 2 - panelHeight / 2;

    graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL, left, top, 0, 0, 176, 40, 256, 256);
    for (int i = 0; i < middles; i++) {
      graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL, left, top + 40 + i * 40, 0, 20, 176, 40, 256, 256);
    }
    graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL, left, top + 40 + middles * 40, 0, 161, 176, 5, 256, 256);

    float titleScale = Math.min(1.0f, 156.0f / Math.max(1, this.font.width(this.title)));
    GuiUtil.renderScaled(graphics, this.title.getString(),
        this.width / 2 - (int) (this.font.width(this.title) * titleScale / 2), top + 8,
        titleScale, 0x404040, false);

    int y = top + 24;
    for (DrawOp op : ops) {
      if (op.wrapped() != null) {
        int color = op.row() instanceof TextRow text ? text.color() : 0x404040;
        graphics.pose().pushMatrix();
        graphics.pose().translate(left + 10, y);
        graphics.pose().scale(0.9f, 0.9f);
        graphics.text(this.font, op.wrapped(), 0, 0, GuiUtil.opaque(color), false);
        graphics.pose().popMatrix();
        y += 12;
      } else if (op.row() instanceof SeparatorRow) {
        graphics.fill(left + 10, y + 2, left + 166, y + 3, 0xFF8B8B8B);
        graphics.fill(left + 10, y + 3, left + 166, y + 4, 0xFFFFFFFF);
        y += 7;
      } else if (op.row() instanceof StatusRow status) {
        graphics.fill(left + 10, y + 1, left + 16, y + 7, 0xFF000000 | status.color());
        graphics.fill(left + 11, y + 2, left + 15, y + 6, 0xFF000000 | brighten(status.color()));
        GuiUtil.renderScaled(graphics, status.text(), left + 20, y, 0.9f, status.color(), false);
        y += 13;
      } else if (op.row() instanceof BarRow bar) {
        GuiUtil.renderScaled(graphics, bar.label(), left + 10, y + 1, 0.9f, 0x404040, false);
        int bx = left + 62;
        int bw = 104;
        int bh = 9;
        graphics.fill(bx - 1, y - 1, bx + bw + 1, y + bh + 1, 0xFF373737);
        graphics.fill(bx, y, bx + bw, y + bh, 0xFF8B8B8B);
        int fillW = (int) (bw * Mth.clamp(bar.ratio(), 0.0f, 1.0f));
        if (fillW > 0) {
          graphics.fill(bx, y, bx + fillW, y + bh, 0xFF000000 | bar.fillColor());
        }
        float valueScale = 0.75f;
        float valueWidth = this.font.width(bar.value()) * valueScale;
        float valueHeight = this.font.lineHeight * valueScale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(bx + bw / 2.0f - valueWidth / 2.0f, y + (bh - valueHeight) / 2.0f + 0.5f);
        graphics.pose().scale(valueScale, valueScale);
        graphics.text(this.font, bar.value(), 0, 0, GuiUtil.opaque(0xFFFFFF), false);
        graphics.pose().popMatrix();
        y += 13;
      }
    }

    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
  }

  private static int brighten(int color) {
    int r = Math.min(255, ((color >> 16) & 0xFF) + 70);
    int g = Math.min(255, ((color >> 8) & 0xFF) + 70);
    int b = Math.min(255, (color & 0xFF) + 70);
    return (r << 16) | (g << 8) | b;
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
