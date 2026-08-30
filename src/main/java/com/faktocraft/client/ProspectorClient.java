package com.faktocraft.client;

import net.minecraft.client.Minecraft;

public final class ProspectorClient {

  private ProspectorClient() {
  }

  public static void openScreen() {
    Minecraft.getInstance().setScreen(new ProspectorScreen());
  }
}
