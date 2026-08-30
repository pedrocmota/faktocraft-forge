package com.faktocraft.common.block.impl.generators.wind_generator;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuWindGenerator extends IndRebMenu {

  public MenuWindGenerator(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuWindGenerator(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M1Registry.WIND_GENERATOR_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
