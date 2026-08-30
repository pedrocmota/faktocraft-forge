package com.faktocraft.common.block.impl.machines.replicator;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M4Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuReplicator extends IndRebMenu {

  public MenuReplicator(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuReplicator(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M4Registry.REPLICATOR_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
