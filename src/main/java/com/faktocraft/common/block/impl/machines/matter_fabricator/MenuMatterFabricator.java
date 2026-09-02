package com.faktocraft.common.block.impl.machines.matter_fabricator;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M4Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuMatterFabricator extends FaktocraftMenu {

  public MenuMatterFabricator(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuMatterFabricator(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M4Registry.MATTER_FABRICATOR_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
