package com.faktocraft.client.screens;

import com.faktocraft.common.block.impl.machines.matter_fabricator.ScreenMatterFabricator;
import com.faktocraft.common.block.impl.machines.replicator.ScreenReplicator;
import com.faktocraft.common.block.impl.machines.scanner.ScreenScanner;
import com.faktocraft.common.registries.machines.M4Registry;
import net.minecraft.client.gui.screens.MenuScreens;

public final class M4Screens {

  public static void register() {
    MenuScreens.register(M4Registry.MATTER_FABRICATOR_MENU, ScreenMatterFabricator::new);
    MenuScreens.register(M4Registry.SCANNER_MENU, ScreenScanner::new);
    MenuScreens.register(M4Registry.REPLICATOR_MENU, ScreenReplicator::new);
  }

  private M4Screens() {
  }
}
