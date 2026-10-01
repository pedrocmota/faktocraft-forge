package com.faktocraft.client.screens;

import com.faktocraft.common.block.impl.machines.matter_fabricator.ScreenMatterFabricator;
import com.faktocraft.common.block.impl.machines.replicator.ScreenReplicator;
import com.faktocraft.common.block.impl.machines.scanner.ScreenScanner;
import com.faktocraft.common.registries.machines.M4Registry;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class M4Screens {

  public static void register(RegisterMenuScreensEvent event) {
    event.register(M4Registry.MATTER_FABRICATOR_MENU, ScreenMatterFabricator::new);
    event.register(M4Registry.SCANNER_MENU, ScreenScanner::new);
    event.register(M4Registry.REPLICATOR_MENU, ScreenReplicator::new);
  }

  private M4Screens() {
  }
}
