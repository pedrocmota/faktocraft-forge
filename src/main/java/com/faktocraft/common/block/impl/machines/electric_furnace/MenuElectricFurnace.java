package com.faktocraft.common.block.impl.machines.electric_furnace;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuElectricFurnace extends IndRebMenu {

  public MenuElectricFurnace(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuElectricFurnace(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M2Registry.ELECTRIC_FURNACE_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
