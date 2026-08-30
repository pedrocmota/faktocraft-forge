package com.faktocraft.common.block.impl.machines.fueling_station;

import com.faktocraft.common.container.IndRebMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuFuelingStation extends IndRebMenu {

  public MenuFuelingStation(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuFuelingStation(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(FuelingStationRegistry.FUELING_STATION_MENU, windowId, level, pos, playerInventory, player);
    this.playerInvTop += 22;
    init(playerInventory);
  }
}
