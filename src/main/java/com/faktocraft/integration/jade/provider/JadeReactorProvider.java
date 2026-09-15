package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor;
import com.faktocraft.common.interfaces.block.IGenerationInfo;
import com.faktocraft.common.screen.bar.GuiHeatBar;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class JadeReactorProvider implements IBlockComponentProvider {

  public static final JadeReactorProvider INSTANCE = new JadeReactorProvider();

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "reactor_info");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    if (!data.contains(JadeReactorDataProvider.TAG_STATUS)) {
      return;
    }
    float heat = data.getFloat(JadeReactorDataProvider.TAG_HEAT);
    int status = data.getInt(JadeReactorDataProvider.TAG_STATUS);
    IElementHelper helper = IElementHelper.get();
    tooltip.add(helper.progress(heat,
        Component.translatable("top." + Faktocraft.MODID + ".reactor_heat", Math.round(heat * 100.0F)),
        helper.progressStyle().color(GuiHeatBar.heatColor(heat)).textColor(0xFFFFFFFF), BoxStyle.DEFAULT, true));
    tooltip.add(Component.translatable(BlockEntityNuclearReactor.statusKey(status))
        .withStyle(GuiHeatBar.statusColor(status)));
    tooltip.add(Component.translatable("top." + Faktocraft.MODID + ".reactor_output",
        IGenerationInfo.rate(data.getInt(JadeReactorDataProvider.TAG_OUTPUT))).withStyle(ChatFormatting.GRAY));
  }
}
