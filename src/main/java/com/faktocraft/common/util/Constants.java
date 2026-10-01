package com.faktocraft.common.util;

import com.faktocraft.Faktocraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public class Constants {

  public static final boolean LEFT_LAYOUT_EXPERIMENT = true;

  public static final Direction[] DIRECTIONS = Direction.values();

  public static final Identifier JEI = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/jei.png");
  public static final Identifier JEI_2 = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/jei_2.png");
  public static final Identifier JEI_LARGE = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/jei_large.png");
  public static final Identifier JEI_LARGE_2 = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/jei_large2.png");
  public static final Identifier COMMON = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/common.png");
  public static final Identifier PROCESS = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/process.png");
  public static final Identifier BUTTONS = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/buttons.png");
}
