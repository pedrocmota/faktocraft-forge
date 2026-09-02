package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.container.FaktocraftMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuLogisticsController extends FaktocraftMenu {

  public MenuLogisticsController(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuLogisticsController(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(LogisticsRegistry.LOGISTICS_CONTROLLER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
