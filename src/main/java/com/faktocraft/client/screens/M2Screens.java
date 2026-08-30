package com.faktocraft.client.screens;

import com.faktocraft.common.block.impl.machines.compressor.ScreenCompressor;
import com.faktocraft.common.block.impl.machines.crusher.ScreenCrusher;
import com.faktocraft.common.block.impl.machines.electric_furnace.ScreenElectricFurnace;
import com.faktocraft.common.block.impl.machines.extractor.ScreenExtractor;
import com.faktocraft.common.block.impl.machines.iron_furnace.ScreenIronFurnace;
import com.faktocraft.common.block.impl.machines.recycler.ScreenRecycler;
import com.faktocraft.common.block.impl.machines.sawmill.ScreenSawmill;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.client.gui.screens.MenuScreens;

public final class M2Screens {

  public static void register() {
    MenuScreens.register(M2Registry.IRON_FURNACE_MENU, ScreenIronFurnace::new);
    MenuScreens.register(M2Registry.ELECTRIC_FURNACE_MENU, ScreenElectricFurnace::new);
    MenuScreens.register(M2Registry.CRUSHER_MENU, ScreenCrusher::new);
    MenuScreens.register(M2Registry.COMPRESSOR_MENU, ScreenCompressor::new);
    MenuScreens.register(M2Registry.EXTRACTOR_MENU, ScreenExtractor::new);
    MenuScreens.register(M2Registry.SAWMILL_MENU, ScreenSawmill::new);
    MenuScreens.register(M2Registry.RECYCLER_MENU, ScreenRecycler::new);
  }

  private M2Screens() {
  }
}
