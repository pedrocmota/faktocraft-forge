package com.faktocraft.common.block.impl.machines.canning_machine;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuCanningMachine extends FaktocraftMenu {

  public MenuCanningMachine(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuCanningMachine(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M3Registry.CANNING_MACHINE_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
