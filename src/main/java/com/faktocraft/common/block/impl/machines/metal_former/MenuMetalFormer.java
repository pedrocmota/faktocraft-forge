package com.faktocraft.common.block.impl.machines.metal_former;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuMetalFormer extends IndRebMenu {

  public MenuMetalFormer(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuMetalFormer(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M3Registry.METAL_FORMER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
