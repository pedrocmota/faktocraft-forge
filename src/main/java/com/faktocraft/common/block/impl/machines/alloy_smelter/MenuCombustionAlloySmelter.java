package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuCombustionAlloySmelter extends FaktocraftMenu {

  public MenuCombustionAlloySmelter(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuCombustionAlloySmelter(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    super(M3Registry.COMBUSTION_ALLOY_SMELTER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
