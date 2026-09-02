package com.faktocraft.common.block.impl.generators.generator;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuGenerator extends FaktocraftMenu {

  public MenuGenerator(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuGenerator(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M1Registry.GENERATOR_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
