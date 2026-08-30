package com.faktocraft.common.block.impl.machines.distillery;

import com.faktocraft.common.container.IndRebMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuDistillery extends IndRebMenu {

  public MenuDistillery(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuDistillery(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(DistilleryRegistry.DISTILLERY_MENU, windowId, level, pos, playerInventory, player);
    this.playerInvTop += 22;
    init(playerInventory);
  }
}
