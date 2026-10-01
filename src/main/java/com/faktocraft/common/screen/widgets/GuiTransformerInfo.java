package com.faktocraft.common.screen.widgets;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.enums.TransformerMode;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.tier.TransformerTier;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;

public class GuiTransformerInfo extends GuiElement {

  public static final int BOX_LEFT = 48;
  public static final int BOX_WIDTH = 80;
  public static final int ROW_INPUT = 21;
  public static final int ROW_LOSS = 37;
  public static final int ROW_OUTPUT = 53;
  private static final int COLOR_LOSS = 0xFFFFA040;
  private static final int COLOR_NO_LOSS = 0xFF55FF55;

  private final TransformerTier tier;
  private final Supplier<TransformerMode> mode;

  public GuiTransformerInfo(IGuiWrapper wrapper, TransformerTier tier, Supplier<TransformerMode> mode) {
    super(wrapper, BOX_WIDTH, ROW_OUTPUT + 10 - ROW_INPUT, BOX_LEFT, ROW_INPUT);
    this.tier = tier;
    this.mode = mode;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    TransformerMode transformerMode = mode.get();
    boolean stepUp = transformerMode == TransformerMode.STEP_UP;
    EnergyTier input = stepUp ? tier.getMinTier() : tier.getMaxTier();
    EnergyTier output = stepUp ? tier.getMaxTier() : tier.getMinTier();
    int loss = tier.getLossPercent(transformerMode);
    int lostPerTick = input.getBasicTransfer() * loss / 100;

    drawRow(graphics, ROW_INPUT, tierText(input), lcdColor(input));
    drawRow(graphics, ROW_LOSS, loss > 0 ? loss + "%  -" + powerText(lostPerTick) : loss + "%",
        loss > 0 ? COLOR_LOSS : COLOR_NO_LOSS);
    drawRow(graphics, ROW_OUTPUT, tierText(output), lcdColor(output));

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  private void drawRow(GuiGraphicsExtractor graphics, int row, String text, int color) {
    float scale = 0.8f;
    int textWidth = GuiUtil.getFont().width(text);
    int maxWidth = BOX_WIDTH - 6;
    if (textWidth * scale > maxWidth) {
      scale = (float) maxWidth / textWidth;
    }
    GuiUtil.renderScaled(graphics, text, getLeftOffset() + 3, getWrapper().getGuiTop() + row + 1, scale, color,
        false);
  }

  private static String tierText(EnergyTier tier) {
    return tier.getLang().getTranslationComponent().getString() + "  " + powerText(tier.getBasicTransfer());
  }

  private static String powerText(int amount) {
    return Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
        TextComponentUtil.getFormattedLong(amount)).getString();
  }

  private static int lcdColor(EnergyTier tier) {
    return switch (tier) {
      case LOW -> 0xFF55FF55;
      case MEDIUM -> 0xFFFFFF55;
      case HIGH -> 0xFFFF5555;
      case VERY_HIGH -> 0xFF7F9FFF;
      case ULTRA -> 0xFFFFAA00;
    };
  }
}
