package com.faktocraft.common.block.impl.machines.fermenter;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuFermenter extends FaktocraftMenu {

  public MenuFermenter(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuFermenter(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M3Registry.FERMENTER_MENU, windowId, level, pos, playerInventory, player);
    this.playerInvTop += 22;
    init(playerInventory);
  }
}
