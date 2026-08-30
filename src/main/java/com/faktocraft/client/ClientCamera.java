package com.faktocraft.client;

import net.minecraft.client.Minecraft;

public final class ClientCamera {

  private ClientCamera() {
  }

  public static boolean isFirstPerson() {
    return Minecraft.getInstance().options.getCameraType().isFirstPerson();
  }
}
