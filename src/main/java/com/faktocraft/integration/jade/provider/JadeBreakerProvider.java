package com.faktocraft.integration.jade.provider;

import com.faktocraft.IndReb;
import com.faktocraft.common.block.impl.cable.BlockBreaker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeBreakerProvider implements IBlockComponentProvider {

  public static final JadeBreakerProvider INSTANCE = new JadeBreakerProvider();

  private static final ResourceLocation UID = new ResourceLocation(IndReb.MODID, "breaker_info");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    var state = accessor.getBlockState();
    if (!state.hasProperty(BlockBreaker.ON)) {
      return;
    }
    boolean on = state.getValue(BlockBreaker.ON);
    Component stateComponent = on
        ? Component.translatable("top." + IndReb.MODID + ".state_on").withStyle(ChatFormatting.GREEN)
        : Component.translatable("top." + IndReb.MODID + ".state_off").withStyle(ChatFormatting.RED);
    tooltip.add(Component.translatable("top." + IndReb.MODID + ".breaker", stateComponent)
        .withStyle(ChatFormatting.GRAY));
  }
}
