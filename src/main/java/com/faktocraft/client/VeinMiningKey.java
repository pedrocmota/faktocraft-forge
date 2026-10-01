package com.faktocraft.client;

import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketVeinMining;
import net.minecraft.client.Minecraft;

public final class VeinMiningKey {

  private static boolean last;

  private VeinMiningKey() {
  }

  public static void tick(Minecraft minecraft) {
    if (minecraft.player == null) {
      last = false;
      return;
    }
    boolean alt = FaktocraftClient.VEIN_MINING_KEY != null && FaktocraftClient.VEIN_MINING_KEY.isDown();
    if (alt != last) {
      last = alt;
      ModNetworking.sendToServer(new PacketVeinMining(alt));
    }
  }

  public static String keyName() {
    return FaktocraftClient.VEIN_MINING_KEY != null
        ? FaktocraftClient.VEIN_MINING_KEY.getTranslatedKeyMessage().getString() : "Alt";
  }

  public static void clear() {
    last = false;
  }
}
