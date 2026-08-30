package com.faktocraft.client;

import com.faktocraft.IndReb;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.network.packet.PacketIEMeterInfo;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.network.chat.Component;

public class IEMeterScreen extends InfoPanelScreen {

  private static final int COLOR_TEXT = 0x404040;
  private static final int COLOR_OK = 0x2E7D32;
  private static final int COLOR_WARN = 0xB02E26;
  private static final int COLOR_IDLE = 0x6B6B6B;
  private static final int FILL_USAGE = 0xC77826;
  private static final int FILL_BUFFER = 0x4CB20D;

  private final PacketIEMeterInfo info;

  public IEMeterScreen(PacketIEMeterInfo info) {
    super(Component.translatable("ie." + IndReb.MODID + (info.network() ? ".network_info" : ".machine_info")));
    this.info = info;
  }

  @Override
  protected void init() {
    super.init();
    rows.clear();
    lines.clear();
    String modid = IndReb.MODID;

    long shortfall = info.netDemand() - (info.network() ? info.netOutput() : 0);

    if (shortfall > 0) {
      rows.add(new StatusRow(Component.translatable("ie." + modid + ".status_bottleneck",
          TextComponentUtil.getFormattedLong(shortfall) + " IE/t").getString(), COLOR_WARN));
    } else if (!info.network() || info.netInput() > 0 || info.netOutput() > 0) {
      rows.add(new StatusRow(Component.translatable("ie." + modid + ".status_ok").getString(), COLOR_OK));
    } else {
      rows.add(new StatusRow(Component.translatable("ie." + modid + ".status_idle").getString(), COLOR_IDLE));
    }
    rows.add(new SeparatorRow());

    if (info.network() && info.voltageLvl() >= 0) {
      EnergyTier current = EnergyTier.getTierFromLvl(Math.min(info.voltageLvl(), 5));
      rows.add(new TextRow(Component.translatable("tooltip." + modid + ".current_voltage",
          current.getLang().getTranslationComponent().withStyle(current.getColor()))
          .append(" (" + TextComponentUtil.getFormattedLong(current.getBasicTransfer()) + " IE/t)"),
          COLOR_TEXT));
    }
    EnergyTier tier = EnergyTier.getTierFromLvl(Math.min(info.tierLvl(), 5));
    String tierKey = info.network() ? "tooltip." + modid + ".max_voltage" : "tooltip." + modid + ".power_tier";
    rows.add(new TextRow(Component.translatable(tierKey,
        tier.getLang().getTranslationComponent().withStyle(tier.getColor()))
        .append(" (" + TextComponentUtil.getFormattedLong(info.transfer()) + " IE/t)"),
        COLOR_TEXT));
    rows.add(new SeparatorRow());

    if (info.network()) {
      rows.add(new TextRow(Component.translatable("ie." + modid + ".flow_line",
          TextComponentUtil.getFormattedLong(info.netInput()),
          TextComponentUtil.getFormattedLong(info.netOutput())).getString(), COLOR_TEXT));
      float usage = info.transfer() > 0 ? (float) info.netOutput() / info.transfer() : 0.0f;
      rows.add(new BarRow(Component.translatable("ie." + modid + ".bar_usage").getString(),
          usage, FILL_USAGE, (int) (usage * 100) + "%"));
      rows.add(new SeparatorRow());
    }

    if (info.showGenerated()) {
      rows.add(new TextRow(Component.translatable("ie." + modid + ".last_generated",
          TextComponentUtil.getFormattedLong(info.lastGenerated()) + " IE/t").getString(), COLOR_TEXT));
      rows.add(new TextRow(Component.translatable("ie." + modid + ".total_generated",
          TextComponentUtil.getFormattedLong(info.totalGenerated()) + " IE").getString(), COLOR_TEXT));
      rows.add(new SeparatorRow());
    }
    if (info.showConsumed()) {
      if (info.receiveRate() > 0) {
        rows.add(new TextRow(Component.translatable("ie." + modid + ".last_consumed_rate",
            TextComponentUtil.getFormattedLong(info.lastConsumed()),
            TextComponentUtil.getFormattedLong(info.receiveRate()) + " IE/t").getString(), COLOR_TEXT));
      } else {
        rows.add(new TextRow(Component.translatable("ie." + modid + ".last_consumed",
            TextComponentUtil.getFormattedLong(info.lastConsumed()) + " IE/t").getString(), COLOR_TEXT));
      }
      rows.add(new TextRow(Component.translatable("ie." + modid + ".total_consumed",
          TextComponentUtil.getFormattedLong(info.totalConsumed()) + " IE").getString(), COLOR_TEXT));
      rows.add(new SeparatorRow());
    }

    if (info.network()) {
      if (info.generators() == 0 && info.machines() == 0 && info.batteries() == 0) {
        rows.add(new TextRow(Component.translatable("ie." + modid + ".connected_none").getString(), COLOR_IDLE));
      } else {
        rows.add(new TextRow(Component.translatable("ie." + modid + ".connected_line",
            String.valueOf(info.generators()), String.valueOf(info.machines()),
            String.valueOf(info.batteries())).getString(), COLOR_TEXT));
      }
    }
    float bufferRatio = info.capacity() > 0 ? (float) info.stored() / info.capacity() : 0.0f;
    rows.add(new BarRow(Component.translatable("ie." + modid + ".bar_buffer").getString(),
        bufferRatio, FILL_BUFFER,
        TextComponentUtil.getFormattedLong(info.stored()) + " / "
            + TextComponentUtil.getFormattedLong(info.capacity()) + " IE"));
  }
}
