package com.faktocraft.client;

import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketProspectorPoll;
import com.faktocraft.common.network.packet.PacketProspectorState;
import com.faktocraft.common.scan.ScanChannels;
import net.minecraft.nbt.CompoundTag;

public final class ProspectorClientData {

  private static int code = ScanChannels.DEFAULT_CODE;
  private static int revision = -1;
  private static CompoundTag scans = new CompoundTag();

  private ProspectorClientData() {
  }

  public static void apply(PacketProspectorState state) {
    code = state.code();
    revision = state.revision();
    scans = state.scans();
  }

  public static String codeText() {
    return ScanChannels.codeText(code);
  }

  public static CompoundTag scans() {
    return scans;
  }

  public static void poll(boolean force) {
    ModNetworking.sendToServer(new PacketProspectorPoll(code, force ? -1 : revision));
  }
}
