package com.faktocraft.common.screen.widgets;

import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.enums.TransformerMode;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.tier.TransformerTier;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;

public class GuiTransformerInfo extends GuiElement {

  private static final int COLOR_STEP_UP = 0xFF21B20D;
  private static final int COLOR_STEP_DOWN = 0xFFB20D0D;

  private final TransformerTier tier;
  private final Supplier<TransformerMode> mode;

  public GuiTransformerInfo(IGuiWrapper wrapper, TransformerTier tier, Supplier<TransformerMode> mode) {
    super(wrapper, 80, 27, 48, 28);
    this.tier = tier;
    this.mode = mode;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    TransformerMode transformerMode = mode.get();

    if (transformerMode == TransformerMode.STEP_UP) {
      GuiUtil.renderScaled(graphics,
          Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
              TextComponentUtil.getFormattedLong(tier.getMinTier().getBasicTransfer())).getString(),
          getLeftOffset() + 3, getTopOffset() + 2, 0.8f, COLOR_STEP_UP, false);
      GuiUtil.renderScaled(graphics,
          Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
              TextComponentUtil.getFormattedLong(tier.getMaxTier().getBasicTransfer())).getString(),
          getLeftOffset() + 3, getTopOffset() + 18, 0.8f, COLOR_STEP_UP, false);
    } else {
      GuiUtil.renderScaled(graphics,
          Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
              TextComponentUtil.getFormattedLong(tier.getMaxTier().getBasicTransfer())).getString(),
          getLeftOffset() + 3, getTopOffset() + 2, 0.8f, COLOR_STEP_DOWN, false);
      GuiUtil.renderScaled(graphics,
          Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
              TextComponentUtil.getFormattedLong(tier.getMinTier().getBasicTransfer())).getString(),
          getLeftOffset() + 3, getTopOffset() + 18, 0.8f, COLOR_STEP_DOWN, false);
    }

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
