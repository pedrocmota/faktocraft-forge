package com.faktocraft.common.block.impl.battery_box;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuBatteryBox extends IndRebMenu {

  public MenuBatteryBox(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuBatteryBox(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M1Registry.BATTERY_BOX_MENU, windowId, level, pos, playerInventory, player);
    this.playerInvTop += 32;
    init(playerInventory);
  }
}
