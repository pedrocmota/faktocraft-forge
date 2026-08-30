package com.faktocraft.common.block.impl.generators.combustion_generator;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuCombustionGenerator extends IndRebMenu {

  public MenuCombustionGenerator(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuCombustionGenerator(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M1Registry.COMBUSTION_GENERATOR_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
