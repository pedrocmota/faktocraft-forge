package com.faktocraft.common.block.impl.machines.polymerizer;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuPolymerizer extends IndRebMenu {

  public MenuPolymerizer(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuPolymerizer(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M3Registry.POLYMERIZER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
