package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.cable.BlockBreaker;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeBreakerProvider implements IBlockComponentProvider {

  public static final JadeBreakerProvider INSTANCE = new JadeBreakerProvider();

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "breaker_info");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    var state = accessor.getBlockState();
    if (state.hasProperty(BlockBreaker.ON)) {
      tooltip.add(WailaData.breakerLine(state.getValue(BlockBreaker.ON)));
    }
  }
}
