package com.faktocraft.client;

import net.minecraft.client.Minecraft;

public final class ProspectorClient {

  private ProspectorClient() {
  }

  public static void openScreen() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player != null) {
      minecraft.setScreen(new ProspectorScreen(minecraft.player.chunkPosition()));
    }
  }
}
