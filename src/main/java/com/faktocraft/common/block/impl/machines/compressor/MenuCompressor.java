package com.faktocraft.common.block.impl.machines.compressor;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuCompressor extends FaktocraftMenu {

  public MenuCompressor(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuCompressor(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M2Registry.COMPRESSOR_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
