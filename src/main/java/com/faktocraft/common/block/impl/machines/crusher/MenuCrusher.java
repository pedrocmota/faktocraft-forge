package com.faktocraft.common.block.impl.machines.crusher;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuCrusher extends IndRebMenu {

  public MenuCrusher(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuCrusher(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M2Registry.CRUSHER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
