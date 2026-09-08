package com.faktocraft.common.interfaces.block;

import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;

public interface IGenerationInfo {

  void appendGenerationInfo(List<Component> tooltip);

  static MutableComponent rate(int perTick) {
    return EnumLang.POWER_TICK.getTranslationComponent(TextComponentUtil.getFormattedLong(perTick))
        .withStyle(ChatFormatting.YELLOW);
  }

  static MutableComponent line(String key, Object... args) {
    return Component.translatable("tooltip.faktocraft." + key, args).withStyle(ChatFormatting.GRAY);
  }
}
