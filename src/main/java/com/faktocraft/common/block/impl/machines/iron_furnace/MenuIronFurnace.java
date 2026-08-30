package com.faktocraft.common.block.impl.machines.iron_furnace;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuIronFurnace extends IndRebMenu {

  public MenuIronFurnace(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuIronFurnace(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M2Registry.IRON_FURNACE_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
