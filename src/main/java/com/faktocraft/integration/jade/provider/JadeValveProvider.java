package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.pipe.IValveHolder;
import com.faktocraft.common.block.impl.pipe.PipeValve;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeValveProvider implements IBlockComponentProvider {

  public static final JadeValveProvider INSTANCE = new JadeValveProvider();

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "valve_info");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    if (!(accessor.getBlockEntity() instanceof IValveHolder holder)) {
      return;
    }
    PipeValve valve = holder.getValve();
    if (valve.isPresent()) {
      tooltip.add(WailaData.valveLine(valve.isOpen()));
    }
  }
}
