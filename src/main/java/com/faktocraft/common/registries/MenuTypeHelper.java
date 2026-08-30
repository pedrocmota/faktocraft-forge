package com.faktocraft.common.registries;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuTypeHelper {

  public static <T extends AbstractContainerMenu> MenuType<T> register(String name,
      RegistrationHandler.PosMenuFactory<T> factory) {
    return RegistrationHandler.menu(name, factory);
  }
}
