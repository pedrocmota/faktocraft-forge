package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class JadeEnergyProvider implements IBlockComponentProvider {

  public static final JadeEnergyProvider INSTANCE = new JadeEnergyProvider();

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "energy_info");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    if (WailaData.maxEnergy(data) > 0) {
      IElementHelper helper = IElementHelper.get();
      tooltip.add(helper.progress(WailaData.ratio(data), WailaData.barText(data),
          helper.progressStyle().color(WailaData.BAR_COLOR), BoxStyle.DEFAULT, true));
    }
    for (Component line : WailaData.energyLines(data)) {
      tooltip.add(line);
    }
  }
}
