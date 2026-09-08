package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeCableProvider implements IBlockComponentProvider {

  public static final JadeCableProvider INSTANCE = new JadeCableProvider();

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "cable_info");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    for (Component line : WailaData.cableLines(accessor.getServerData())) {
      tooltip.add(line);
    }
  }
}
