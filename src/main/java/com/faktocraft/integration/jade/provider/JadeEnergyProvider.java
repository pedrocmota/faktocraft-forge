package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.ui.SimpleProgressStyle;

public class JadeEnergyProvider implements IBlockComponentProvider {
  public static final JadeEnergyProvider INSTANCE = new JadeEnergyProvider();

  private static final Identifier UID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "energy_info");

  @Override
  public Identifier getUid() {
    return UID;
  }

  static ProgressView progress(float ratio, Component text, int color, boolean forceWhiteText) {
    ProgressStyle style = JadeUI.progressStyle().canDecrease(true);
    if (style instanceof SimpleProgressStyle simple) {
      simple.color = color;
      if (forceWhiteText) {
        simple.autoTextColor = false;
      }
    }
    return new ProgressView(ProgressView.Part.of(ratio, color), text, style, BoxStyle.nestedBox());
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    if (WailaData.maxEnergy(data) > 0) {
      tooltip.add(JadeUI.progress(progress(WailaData.ratio(data), WailaData.barText(data), WailaData.BAR_COLOR,
          false)));
    }
    for (Component line : WailaData.energyLines(data)) {
      tooltip.add(line);
    }
  }
}
