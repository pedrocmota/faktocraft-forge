package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuAlloySmelter extends FaktocraftMenu {

  public MenuAlloySmelter(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuAlloySmelter(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M3Registry.ALLOY_SMELTER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
