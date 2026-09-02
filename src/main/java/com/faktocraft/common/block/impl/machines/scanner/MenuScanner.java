package com.faktocraft.common.block.impl.machines.scanner;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M4Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuScanner extends FaktocraftMenu {

  public MenuScanner(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuScanner(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M4Registry.SCANNER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
