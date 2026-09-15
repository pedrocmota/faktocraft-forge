package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import com.faktocraft.common.container.FaktocraftMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuNuclearReactor extends FaktocraftMenu {

  public MenuNuclearReactor(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(NuclearReactorRegistry.NUCLEAR_REACTOR_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
