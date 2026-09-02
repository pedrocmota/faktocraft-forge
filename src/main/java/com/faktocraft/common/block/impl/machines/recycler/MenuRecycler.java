package com.faktocraft.common.block.impl.machines.recycler;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuRecycler extends FaktocraftMenu {

  public MenuRecycler(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuRecycler(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M2Registry.RECYCLER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
